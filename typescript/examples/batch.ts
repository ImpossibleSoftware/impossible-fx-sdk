import { ImpossibleFX } from '../src/index.js';

async function main() {
  const client = new ImpossibleFX({
    region: 'us-east-1',
    apiKey: process.env.IMPOSSIBLE_API_KEY,
  });

  // Create a batch
  const batch = await client.createBatch({ routing: 'high-priority' });
  console.log('Batch created:', batch.id);

  // Add tasks to the batch
  await client.addBatchTasks(batch.id, [
    {
      movie: 'product-card',
      params: { name: 'Widget A', price: '$9.99', image: 'widget-a.png' },
      format: 'png',
    },
    {
      movie: 'product-card',
      params: { name: 'Widget B', price: '$19.99', image: 'widget-b.png' },
      format: 'png',
    },
    {
      movie: 'product-card',
      params: { name: 'Widget C', price: '$29.99', image: 'widget-c.png' },
      format: 'png',
    },
  ]);
  console.log('Tasks added to batch');

  // Start the batch
  await client.runBatch(batch.id);
  console.log('Batch started');

  // Poll for batch status
  let status = await client.getBatchStatus(batch.id);
  while (status.status !== 'completed' && status.status !== 'failed') {
    console.log(`Batch status: ${status.status} (${status.tasks} tasks)`);
    await new Promise((resolve) => setTimeout(resolve, 2000));
    status = await client.getBatchStatus(batch.id);
  }

  if (status.status === 'failed') {
    console.error('Batch failed');
    return;
  }

  console.log('Batch completed!');

  // Get results
  const results = await client.getBatchResults(batch.id);
  for (const result of results) {
    console.log(`  Token: ${result.token} | Status: ${result.status} | URL: ${result.url}`);
  }
}

main().catch(console.error);
