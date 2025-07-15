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

function handleLineItemChange(event) {
    console.log("Event target: ", event.target);
    const row = event.target.closest("tr");

    if (!row) {
        console.error("Parent row (<tr>) not found. Ensure that the event is triggered from within a table row.");
        return;
    }

    const productSelect = row.querySelector(".product-select");
    const selectedProductId = productSelect.value;

    // Quietly skip backend logic if no product is selected
    if (!selectedProductId) {
        return;
    }

    const uuid = row.querySelector(".line-item-uuid").value;
    const submitted = row.querySelector(".line-item-submitted").value === "true";
    const quantityInput = row.querySelector(".quantity-input").value;
    const priceCell = row.querySelector(".price-cell");
    const subtotalCell = row.querySelector(".subtotal-cell");

    // Get product price (fixed unit price) and quantity
    const productPrice = parseFloat(productSelect.selectedOptions[0]?.dataset.price || 0);
    const quantity = parseInt(quantityInput, 10) || 1;

    // --- FIX: Update Price column with the fixed unit price ---
    priceCell.textContent = `$${productPrice.toFixed(2)}`;

    // Update Subtotal column with the calculated value
    const lineItemSubtotal = productPrice * quantity;
    subtotalCell.textContent = `$${lineItemSubtotal.toFixed(2)}`;

    // Log debug information
    console.log("Fixed Product Price:", productPrice);
    console.log("Quantity:", quantity);
    console.log("Calculated Subtotal:", lineItemSubtotal);

    // Update the total order cost
    updateTotal();

    // Determine the appropriate action for the backend
    if (submitted) {
        // Update an already submitted line item
        updateLineItem(uuid, selectedProductId, quantity, productPrice);
    } else {
        // Add a new line item
        addNewLineItem(uuid, selectedProductId, quantity, productPrice, row);
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


async function updateLineItem(uuid, productId, quantity, price) {
    console.log("Updating line item to backend with uuid:{}, productID:{}, quantity:{}, and price{}", uuid, productId, quantity, price);
    try {
        // Calculate the new subtotal for the line item
        const subtotal = quantity * price;

        // Update the line item on the server
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

            // Update the corresponding line item's subtotal in the DOM
            const row = document.querySelector(`[data-line-item-uuid="${uuid}"]`);
            if (row) {
                const subtotalCell = row.querySelector(".subtotal-cell");
                subtotalCell.textContent = `$${subtotal.toFixed(2)}`;
            }

            // Recalculate the total after updating the subtotal
            updateTotal();
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
        <td class="subtotal-cell">$0.00</td>
        <td>
            <button onclick="removeLineItem(event)">Remove</button>
            <input type="hidden" class="line-item-uuid" value="${lineItemUUID}">
            <input type="hidden" class="line-item-submitted" value="false">
        </td>
    `;
    tableBody.appendChild(newRow);
    updateDropdowns();
}

function updatePrice(event) {
    const row = event.target.closest("tr");
    const productSelect = row.querySelector(".product-select");
    const quantityInput = row.querySelector(".quantity-input");
    const priceCell = row.querySelector(".price-cell");
    const subtotalCell = row.querySelector(".subtotal-cell");

    // Get unit price from selected product
    const selectedOption = productSelect.selectedOptions[0];
    const productPrice = parseFloat(selectedOption?.dataset.price || 0); // Fixed unit price

    // Log debug information
    console.log("Product Price (Fixed):", productPrice);
    console.log("Current Quantity:", quantityInput.value);

    // Ensure the Price cell ALWAYS displays fixed unit price
    priceCell.textContent = `$${productPrice.toFixed(2)}`;
    console.log("Price Cell Content (After Fix):", priceCell.textContent);

    // Calculate and update subtotal
    const quantity = parseInt(quantityInput.value, 10) || 1; // Default to 1 if invalid
    const lineItemSubtotal = productPrice * quantity;

    console.log("Calculated Subtotal (Price × Quantity):", lineItemSubtotal);
    subtotalCell.textContent = `$${lineItemSubtotal.toFixed(2)}`;

    // Log cell values after update
    console.log("Subtotal Cell Content (After Update):", subtotalCell.textContent);

    // Update the grand total
    updateTotal();
}

function updateTotal() {
    const subtotalCells = document.querySelectorAll(".subtotal-cell");
    const totalElement = document.getElementById("order-total");
    let total = 0;

    subtotalCells.forEach((cell) => {
        const subtotal = parseFloat(cell.textContent.replace("$", "")) || 0;
        total += subtotal;
    });

    totalElement.textContent = `Total: $${total.toFixed(2)}`;
}

function removeRow(event) {
    const row = event.target.closest("tr");
    if (row) {
        row.remove();
        updateTotal(); // Update total after removing a row
    }
}

// Add event listeners for quantity changes and row removal
document.addEventListener("input", (event) => {
    if (event.target.classList.contains("quantity-input")) {
        updatePrice(event);
    }
});

document.addEventListener("change", (event) => {
    if (event.target.classList.contains("product-select")) {
        updatePrice(event);
    }
});

document.addEventListener("click", (event) => {
    if (event.target.classList.contains("delete-button")) {
        removeRow(event);
    }
});



// Submit the entire order to the backend
function submitOrder() {
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
