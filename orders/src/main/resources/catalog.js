

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

// Add a new line item to the table
function addLineItem() {
    const tableBody = document.querySelector("#order-table tbody");
    const newRow = document.createElement("tr");
    newRow.innerHTML = `
        <td>${generateProductDropdown()}</td>
        <td>
            <input type="number" value="1" min="1" class="quantity-input" onchange="validateQuantity(event)">
        </td>
        <td class="price-cell">$0.00</td>
        <td>
            <button onclick="deleteLineItem(event)">Remove</button>
        </td>
    `;
    tableBody.appendChild(newRow);
    updateDropdowns();
}

// Validate and update the quantity input
function validateQuantity(event) {
    const input = event.target;
    const quantity = parseInt(input.value, 10);

    if (isNaN(quantity) || quantity < 1) {
        alert("Quantity must be a positive number greater than or equal to 1.");
        input.value = 1; // Reset to the minimum valid quantity
    }

    // Trigger price recalculation
    updatePrice(event);
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

// Delete a line item from the table
function deleteLineItem(event) {
    const row = event.target.closest("tr");
    row.remove();
    updateTotal();
    updateDropdowns();
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
        const selectedProductId = productSelect.value;
        productSelect.innerHTML = generateProductDropdown(selectedProductId);
    });
}

// Initialize the page by adding the first line item
document.addEventListener("DOMContentLoaded", () => {
    addLineItem(); // Add the first row by default
});

// Function to Add Item to Order
async function addToOrder(productId) {
    const quantityInput = document.getElementById(`qty-${productId}`);
    const quantity = parseInt(quantityInput.value, 10);

    if (quantity <= 0) {
        alert("Quantity must be at least 1.");
        return;
    }

    try {
        const response = await fetch("/order/add", {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded"
            },
            body: new URLSearchParams({
                id: productId,
                quantity: quantity
            })
        });

        if (!response.ok) throw new Error("Failed to add item to order.");
        alert("Item added to order!");
        refreshOrderInfo(); // Optionally refresh the UI
    } catch (error) {
        alert(error.message);
    }
}


// Function to Refresh Order Information
// async function refreshOrderInfo() {
//     try {
//         const response = await fetch(`/order.json?id=${currentOrderId}`);
//         if (!response.ok) throw new Error("Failed to refresh order information.");
//         const order = await response.json();
//         updateOrderUI(order);
//     } catch (error) {
//         alert(error.message);
//     }
// }

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

