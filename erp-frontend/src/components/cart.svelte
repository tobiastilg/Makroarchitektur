<script>
	let cart = []
	let products = [
		{id: 1, image: "src/img/hamilton.jpg", productNumber: 6943953142, productName: "Hamilton Khaki Field", priceNet: 437.5, tax: 20, amount: 1},
		{id: 2, image: "src/img/omega.jpg", productNumber: 2109042200, productName: "Omega Seamaster Diver 300m", priceNet: 7000, tax: 20, amount: 1},
		{id: 3, image: "src/img/rolex.png", productNumber: 2265701337, productName: "Rolex Explorer II", priceNet: 11250, tax: 20, amount: 1},
	]

	let customer = {
	customerID: "4815162342",
	customerFirstname: "Tobias",
	customerLastname: "Tilg",
	customerEmail: "testitest@test.com",
	customerStreet: "Brennbichl 25",
	customerZipcode: "6460",
	customerCity: "Imst",
	customerCountry: "Austria",
	cartItems: [],
	}

	async function buyProducts() {
		if (cart.length == 0) return
		customer.cartItems = cart
		const response = await fetch("http://localhost:8080/api/v1/orders/", {
			method: "POST",
			headers: {
				"Content-Type": "application/json",
			},
			body: JSON.stringify(customer),
		})
		cart = [];
		//const jsonResponse = await response.json();
		//console.log(jsonResponse)
	}

	const addToCart = (product) => {
		for(let item of cart) {
				if(item.id === product.id) {
					product.amount += 1
					cart = cart;
					return;
				}
		}
		cart = [...cart, product]
	}
	
	const minusItem = (product) => {
		for(let item of cart) {
				if(item.id === product.id) {
					if(product.amount > 1 ) {
							product.amount -= 1
							cart = cart
					} else {
							cart = cart.filter((cartItem) => cartItem != product)
					}
					return;
				}
		}
	}
	
	const plusItem = (product) => {
		for(let item of cart) {
			if(item.id === product.id) {
				item.amount += 1
				cart = cart;
				return;
			}
		}
	}

	$: total = cart.reduce((sum, item) => sum + item.priceNet*((item.tax/100)+1) * item.amount, 0)

	async function buy() {
		console.log("Kaufen...")
	}

</script>

<div class="m-3">

	<div class="mb-5">
		<h2 class="m-3 mb-5">Produkte</h2>
		<div class="text-center row">
			{#each products as product}
			<div class="mt-3 col-sm-4 border-right">
				<h5>{product.productName}</h5>
				<p>Preis: {product.priceNet*((product.tax/100)+1)}€</p>
				<img src="{product.image}" alt="{product.productName}" class="rounded" height=250px>
				<div></div>
				<button class="btn btn-secondary mr-2 mb-2" on:click={() => addToCart(product)}>Zum Warenkorb</button>
			</div>
			{/each}
		</div>
	</div>

	<div class="border-top">
		<h2 class="m-3">Warenkorb</h2>
		<div class="text-center row">
			{#each cart as item}
				{#if item.amount > 0}
				<div class="mt-3 col-sm-1">
					<img height="100px" src={item.image} alt={item.productName}/>
					<div>{item.amount}
						<button class="rounded btn btn-light" on:click={() => plusItem(item)}>+</button>
						<button class="rounded btn btn-light" on:click={() => minusItem(item)}>-</button>
					</div>
					<p>€{item.priceNet*((item.tax/100)+1) * item.amount}</p>
				</div>
				{/if}
			{/each}
		</div>
		<div class="m-3">
			<h4>Summe: € {total}</h4>
			<button class="btn btn-dark {cart.length == 0 ? 'disabled' :''}" on:click={buyProducts}>Kaufen</button>
		</div>
	</div>
	
</div>