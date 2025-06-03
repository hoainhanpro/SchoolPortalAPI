<script>
  import { onMount } from 'svelte';
  import { browser } from '$app/environment';
  import 'bootstrap/dist/css/bootstrap.min.css';
  
  onMount(() => {
    if (browser) {
      import('bootstrap/dist/js/bootstrap.bundle.min.js');
    }
  });
</script>

<svelte:head>
  <title>School Portal</title>
  <meta name="description" content="School Portal System" />
</svelte:head>

<slot />

<style>
  :global(body) {
    font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  }
  
  :global(.btn-primary) {
    background-color: #0d6efd;
    border-color: #0d6efd;
  }
  
  :global(.bg-primary) {
    background-color: #0d6efd !important;
  }
  
  :global(.text-primary) {
    color: #0d6efd !important;
  }
  
  /* Removed Bootstrap Icons import from here as it's now in app.html */
</style>