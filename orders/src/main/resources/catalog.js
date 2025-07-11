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
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ items: orderItems })
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

// Utility Function: Get OrderId from cookies
function getOrderId() {
    const name = "eda-orderId=";
    const decodedCookie = decodeURIComponent(document.cookie);
    const cookies = decodedCookie.split(';');
    for (const cookie of cookies) {
        let c = cookie.trim();
        if (c.indexOf(name) === 0) {
            return c.substring(name.length, c.length);
        }
    }
    throw new Error("Order ID not found in cookies.");
}

// Function to Load Product Catalog
async function loadCatalog() {
    const res = await fetch('/catalog.json');
    const products = await res.json();
    const catalog = document.getElementById('catalog');
    catalog.innerHTML = products.map(p => `
      <div>
        <strong>${p.name}</strong> - $${p.price.toFixed(2)}
        <input type="number" id="qty-${p.id}" min="1" value="1">
        <button onclick="addToOrder(${p.id})">Add</button>
      </div>
    `).join('');
}

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


// Function to Refresh Order Information (Optional but Recommended)
async function refreshOrderInfo() {
    try {
        const response = await fetch("/order.json");
        if (!response.ok) throw new Error("Failed to refresh order information.");

        const order = await response.json();
        updateOrderUI(order);
    } catch (error) {
        alert(error.message);
    }
}

// Function to Update the UI with Order Data
function updateOrderUI(order) {
    const orderTable = document.querySelector("#order-table tbody");
    orderTable.innerHTML = ""; // Clear current content
    order.items.forEach(item => {
        const row = document.createElement("tr");
        row.innerHTML = `
            <td>${item["product"].id}</td>
            <td>${item["product"].name}</td>
            <td>${item.quantity}</td>
            <td>${item["product"].price.toFixed(2)}</td>
            <td>${(item.quantity * item["product"].price).toFixed(2)}</td>
            <td><button onclick="deleteLineItem(${item["product"].id})">Remove</button></td>
        `;
        orderTable.appendChild(row);
    });

    // Update the order total
    document.getElementById("order-total").textContent = `$${order.total.toFixed(2)}`;
}

function loadOrderId() {
    function getCookieValue(cookieName) {
        const cookies = document.cookie.split("; ");
        for (let cookie of cookies) {
            const [name, value] = cookie.split("=");
            if (name === cookieName) {
                return value;
            }
        }
        return null; // Return null if the cookie is not found
    }

    currentOrderId = getCookieValue("eda-orderId");

    if (currentOrderId) {
        console.log("Order ID:", currentOrderId);
        // Update the <h1> element with the OrderId
        const heading = document.querySelector("Th1");
        if (heading) {
            heading.textContent = `Create Your Order: ${currentOrderId}`;
        }
    } else {
        console.log("Order ID cookie not found.");
    }
}

// Wrapper function to call both loadCatalog and loadOrderId
function initializePage() {
    console.log("Initializing Catalog");
    loadCatalog();
    console.log("Initializing Order ID" + currentOrderId);
    loadOrderId();
}



// Assign the wrapper function to window.onload
window.onload = initializePage;

