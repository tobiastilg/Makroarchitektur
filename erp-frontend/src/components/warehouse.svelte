<script>
    import { onMount } from "svelte"

    let packings = []
    async function getPackings() {
        const response = await fetch("http://localhost:8080/stock/packings/");
        packings = await response.json();
        return packings;
    }

    let promise = getPackings();

    onMount(async function () {
		promise = getPackings();
	})

    async function pack(packingItem) {
        if (packingItem.packed) return
        const response = await fetch("http://localhost:8080/stock/setPackedForPacking/" + packingItem.id, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
        });
        let promise = getPackings();
    }
</script>

<div class="m-3">
    <div class="m-3 mb-3">
    <h2 class="mb-3">Lager</h2>
        {#await promise}
            <p>Warte auf Bestellungen...</p>
        {:then}
            {#each packings as packingItem}
                <h5>Bestellung: {packingItem.orderId}</h5>
                <div class="mb-2">Kunde: {packingItem.deliveryData.name}</div>
                <div>Adresse: {packingItem.deliveryData.street}, {packingItem.deliveryData.zipcode} {packingItem.deliveryData.city}, {packingItem.deliveryData.country}</div>
                
                <table class="table table-striped mt-2 mb-5">
                    <thead>
                        <tr>
                        <th scope="col" style="width: 15%">Produktnummer</th>
                        <th scope="col" style="width: 25%">Produktname</th>
                        <th scope="col" style="width: 15%">Anzahl</th>
                        <th scope="col" style="width: 20%">Bestellstatus</th>
                        <th scope="col" style="width: 25%"/>
                        </tr>
                    </thead>
                    <tbody>
                        {#each packingItem.packingItemList as item}
                            <tr>
                                <td>{item.productNumber}</td>
                                <td>{item.productName}</td>
                                <td>{item.amount}</td>
                                <td class="{item.packed ? 'font-weight-bold': 'font-italic'}">{item.packed ? "Verpackt": "Bestellt"}</td>
                                <td>
                                    <button class="btn btn-secondary {item.packed ? 'disabled': ''}" 
                                    on:click={() => pack(item)}>Verpacken</button>
                                </td>
                            </tr>
                        {/each}
                    </tbody>
                </table>
            {/each}
        {:catch error}
            <p style="color: red">{error.message}</p>
        {/await}
    </div>
</div>