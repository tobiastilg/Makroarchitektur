<script>
  import { Router, Route, Link } from "svelte-navigator"; //Routing

	import Cart from './components/cart.svelte';
  import Accounting from './components/accounting.svelte';
  import Warehouse from './components/warehouse.svelte';
</script>

<main>
  <Router>
    <nav class="navbar navbar-expand-lg navbar-light bg-light sticky-top">
      <div class="collapse navbar-collapse" id="navbarNav">
        <ul class="navbar-nav">
          <li class="nav-item active">
            <Link to="/" class="nav-link">Warenkorb</Link>
          </li>
          <li class="nav-item active">
            <Link to="accounting" class="nav-link">Buchhaltung</Link>
          </li>
          <li class="nav-item active">
            <Link to="warehouse" class="nav-link">Lager</Link>
          </li>
        </ul>
      </div>
    </nav>
    <Route path="/" component={Cart} />
    <Route path="accounting" component={Accounting} />
    <Route path="warehouse" component={Warehouse} />
  </Router>
</main>