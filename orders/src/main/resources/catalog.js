

const products = [
    { id: 1, name: "Laptop", price: 1000 },
    { id: 2, name: "Headphones", price: 200 },
    { id: 3, name: "Keyboard", price: 150 },
    { id: 4, name: "Mouse", price: 80 },
    { id: 5, name: "Monitor", price: 300 }
];

let currentOrderId = null;

// Generate product dropdown dynamically with selected product filtering
function generateProductDropdown(selectedProductId = null) {
    const selectedProductIds = Array.from(document.querySelectorAll(".product-select"))
        .map((select) => select.value)
        .filter((value) => value);

    return `
        <select class="product-select" onchange="handleProductChange(event)">
            <option value="">Select a Product</option>
            ${products
                .filter((product) => !selectedProductIds.includes(String(product.id)) || String(product.id) === selectedProductId)
                .map(
                    (product) =>
                        `<option value="${product.id}" data-price="${product.price}" ${
                            product.id === Number(selectedProductId) ? "selected" : ""
                        }>${product.name}</option>`
                )
                .join("")}
        </select>
    `;
}

// Handle changes to a product or quantity in a line item
function handleLineItemChange(event) {
    console.log("Event target: ", event.target);
    const row = event.target.closest("tr");

    if (!row) {
        console.error("Parent row (<tr>) not found. Ensure that the event is triggered from within a table row.");
        return;
    }

    const uuid = row.querySelector(".line-item-uuid").value;
    const submitted = row.querySelector(".line-item-submitted").value === "true";
    const productSelect = row.querySelector(".product-select");
    const selectedProductId = productSelect.value;
    const quantityInput = row.querySelector(".quantity-input").value;
    const priceCell = row.querySelector(".price-cell");

    const productPrice = parseFloat(productSelect.selectedOptions[0]?.dataset.price || 0);
    const quantity = parseInt(quantityInput, 10) || 1;
    const lineItemPrice = productPrice * quantity;

    // Update the price shown in the row
    priceCell.textContent = `$${lineItemPrice.toFixed(2)}`;
    updateTotal();

    // Determine the appropriate action
    if (submitted) {
        updateLineItem(uuid, selectedProductId, quantity, lineItemPrice);
    } else {
        addNewLineItem(uuid, selectedProductId, quantity, lineItemPrice, row);
    }
}



// Send `POST` request to add a new line item to the backend
async function addNewLineItem(uuid, productId, quantity, price, row) {
    try {
        const response = await fetch(`/order/${currentOrderId}/items`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                id: uuid,
                productId,
                quantity,
                price,
            }),
        });

        if (response.ok) {
            // Successfully submitted; mark this line item as submitted
            row.querySelector(".line-item-submitted").value = "true";
            console.log(`Line item ${uuid} successfully added.`);
        } else {
            throw new Error(`Failed to add line item ${uuid}`);
        }
    } catch (error) {
        console.error(error.message);
        alert("Error adding new line item. Please try again.");
    }
}


// Send a ` PATCH ` request to update an existing line item
async function updateLineItem(uuid, productId, quantity, price) {
    try {
        const response = await fetch(`/order/${currentOrderId}/items/${uuid}`, {
            method: "PATCH",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                productId,
                quantity,
                price,
            }),
        });

        if (response.ok) {
            console.log(`Line item ${uuid} successfully updated.`);
        } else {
            throw new Error(`Failed to update line item ${uuid}`);
        }
    } catch (error) {
        console.error(error.message);
        alert("Error updating line item. Please try again.");
    }
}


// Add a new line item to the table
function addLineItem() {
    const tableBody = document.querySelector("#order-table tbody");
    const newRow = document.createElement("tr");

    // Generate a UUID for this line item
    const lineItemUUID = crypto.randomUUID();

    newRow.innerHTML = `
        <td>${generateProductDropdown()}</td>
        <td>
            <input type="number" value="1" min="1" class="quantity-input" onchange="handleLineItemChange(event)">
        </td>
        <td class="price-cell">$0.00</td>
        <td>
            <button onclick="removeLineItem(event)">Remove</button>
            <input type="hidden" class="line-item-uuid" value="${lineItemUUID}">
            <input type="hidden" class="line-item-submitted" value="false">
        </td>
    `;
    tableBody.appendChild(newRow);
    updateDropdowns();
}

// Update the price for a line item and recalculate totals
function updatePrice(event) {
    const row = event.target.closest("tr");
    const productSelect = row.querySelector(".product-select");
    const quantityInput = row.querySelector(".quantity-input");
    const priceCell = row.querySelector(".price-cell");

    const selectedOption = productSelect.selectedOptions[0];
    const productPrice = parseFloat(selectedOption?.dataset.price || 0);
    const quantity = parseInt(quantityInput.value, 10) || 1;

    // Update line item price
    const lineItemPrice = productPrice * quantity;
    priceCell.textContent = `$${lineItemPrice.toFixed(2)}`;

    // Update total
    updateTotal();
}

// Update the total price for all line items
function updateTotal() {
    const priceCells = document.querySelectorAll(".price-cell");
    const totalElement = document.getElementById("order-total");
    let total = 0;

    priceCells.forEach((cell) => {
        const price = parseFloat(cell.textContent.replace("$", "")) || 0;
        total += price;
    });

    totalElement.textContent = `$${total.toFixed(2)}`;
}

// Submit the entire order to the backend
// TODO: I don't need the elements here remove rows.forEach soon
function submitOrder() {
    const orderItems = [];
    const rows = document.querySelectorAll("#order-table tbody tr");

    rows.forEach((row) => {
        const productSelect = row.querySelector(".product-select");
        const quantityInput = row.querySelector(".quantity-input");
        const priceCell = row.querySelector(".price-cell");

        const productId = productSelect.value;
        const quantity = parseInt(quantityInput.value, 10) || 1;
        const price = parseFloat(priceCell.textContent.replace("$", ""));

        if (productId) {
            orderItems.push({ productId, quantity, price });
        }
    });

    if (orderItems.length === 0) {
        alert("Please add at least one valid line item before submitting the order.");
        return;
    }

    // Submit the order to the backend
    fetch("/order/submit", {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify({orderId: currentOrderId})
    })
        .then((response) => {
            if (response.ok) {
                window.location = "/thank-you.html";
            } else {
                throw new Error("Failed to submit the order. Please try again.");
            }
        })
        .catch((error) => alert(error.message));
}

// Handle the removal of a line item
function removeLineItem(event) {
    const row = event.target.closest("tr");
    const uuid = row.querySelector(".line-item-uuid").value;
    const submitted = row.querySelector(".line-item-submitted").value === "true";

    if (submitted) {
        // If the item is submitted, send a DELETE request
        deleteLineItem(uuid).then(() => {
            row.remove();
            updateTotal();
            updateDropdowns();
        });
    } else {
        // If not submitted, simply remove the row
        row.remove();
        updateTotal();
        updateDropdowns();
    }
}

// Send `DELETE` request to remove an existing line item
async function deleteLineItem(uuid) {
    try {
        const response = await fetch(`/order/${currentOrderId}/items/${uuid}`, {
            method: "DELETE",
        });

        if (response.ok) {
            console.log(`Line item ${uuid} successfully deleted.`);
        } else {
            throw new Error(`Failed to delete line item ${uuid}`);
        }
    } catch (error) {
        console.error(error.message);
        alert("Error deleting line item. Please try again.");
    }
}

// Handle product changes to refresh the dropdowns and update prices
function handleProductChange(event) {
    updatePrice(event);
    updateDropdowns();
}

// Refresh dropdown menus to prevent duplicate product selection across rows
function updateDropdowns() {
    const rows = document.querySelectorAll("#order-table tbody tr");
    rows.forEach((row) => {
        const productSelect = row.querySelector(".product-select");
        productSelect.setAttribute("onchange", "handleLineItemChange(event)");
        const selectedProductId = productSelect.value;
        productSelect.innerHTML = generateProductDropdown(selectedProductId);
    });
}

// Initialize the page by adding the first line item
document.addEventListener("DOMContentLoaded", () => {
    addLineItem(); // Add the first row by default
});

function displayOrderId() {
    // Update the header page with the orderID
    const heading = document.querySelector("h1");
    if (heading) {
        heading.textContent = `Create Your Order: ${currentOrderId}`;
    } else {
        heading.textContent = `Create Your Order`;
    }
}

function createUUID() {
    console.log("Creating a UUID");
    // Check if crypto.randomUUID is available, if not provide a fallback
    if (!crypto?.randomUUID) {
        crypto.randomUUID = function () {
            return ([1e7] + -1e3 + -4e3 + -8e3 + -1e11).replace(/[018]/g, c =>
                (c ^ crypto.getRandomValues(new Uint8Array(1))[0] & 15 >> c / 4).toString(16)
            );
        };
    }
    currentOrderId = crypto.randomUUID();
}

function createOrder(currentOrderId) {
    fetch("/order", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({orderId: currentOrderId})
    })
        .then(response => {
            if (!response.ok) {
                throw new Error("Failed to create order");
            }
            console.log("Order created successfully with ID:", currentOrderId);
        })
        .catch(error => {
            console.error("Error creating order:", error);
            alert("Failed to create order. Please try again.");
        });
}

// Wrapper function to call both loadCatalog and loadOrderId
function initializePage() {
    console.log("Initializing Page");
    createUUID();
    displayOrderId();
    createOrder(currentOrderId);
}

// Assign the wrapper function to window.onload
window.onload = initializePage;

