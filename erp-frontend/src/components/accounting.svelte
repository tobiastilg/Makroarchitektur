<script>
    import { onMount } from "svelte"

    let orders = []
    async function getOrders() {
        const response = await fetch("http://localhost:8080/api/v1/orders/");
        orders = await response.json();
        return orders;
    }

    let promise = getOrders();

    onMount(async function () {
		promise = getOrders();
	})

    async function verifyPayment(order) {
        if (order.state != 'PLACED') return
        const response = await fetch("http://localhost:8080/api/v1/orders/checkpayment/" + order.orderID, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
        });
        let promise = getOrders();
    }
</script>

<div class="m-3">
    <div class="mb-5 m-3">
		<h2>Buchhaltung</h2>
        <table class="table table-striped">
            <thead>
                <tr>
                  <th scope="col">Bestellnummer</th>
                  <th scope="col">Kundennummer</th>
                  <th scope="col">Bestellstatus</th>
                  <th scope="col"/>
                </tr>
            </thead>
            <tbody>
                {#await promise}
                    <p>Warte auf Bestellungen...</p>
                {:then}
                    {#each orders as order}
                        <tr>
                            <th scope="row">{order.orderID}</th>
                            <td>{order.customerID}</td>
                            <td class="{order.state != 'PLACED' ? 'font-weight-bold': 'font-italic'}">{order.state != 'PLACED' ? "Bezahlt": "Bestellt"}</td>
                            <td>
                                <button class="btn btn-secondary {order.state != 'PLACED' ? 'disabled': ''}" 
                                on:click={() => verifyPayment(order)}>Zahlung akzeptieren</button>
                            </td>
                        </tr>
                    {/each}
                {:catch error}
                    <p style="color: red">{error.message}</p>
                {/await}
            </tbody>
        </table>
    </div>
</div>