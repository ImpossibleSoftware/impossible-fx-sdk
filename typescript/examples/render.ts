import { ImpossibleFX } from '../src/index.js';

async function main() {
  const client = new ImpossibleFX({
    region: 'us-east-1',
    apiKey: process.env.IMPOSSIBLE_API_KEY,
  });

  // Create a token for the project
  const tokenResult = await client.createToken('my-project', 'intro-video', {
    title: 'Welcome',
    subtitle: 'Getting Started with Impossible FX',
    color: '#ff6600',
  });

  console.log('Token created:', tokenResult.token);

  // Synchronous render — waits for completion and returns the URL
  const result = await client.render('my-project', 'intro-video', {
    title: 'Welcome',
    subtitle: 'Getting Started with Impossible FX',
    color: '#ff6600',
  }, {
    format: 'mp4',
  });

  console.log('Render complete!');
  console.log('Token:', result.token);
  console.log('URL:', result.url);
  console.log('Expires:', new Date(result.expires * 1000).toISOString());

  // Async render — returns immediately, poll for progress
  const asyncResult = await client.render('my-project', 'intro-video', {
    title: 'Async Render',
  }, {
    format: 'mp4',
    async: true,
  });

  console.log('\nAsync render started:', asyncResult.token);

  // Build the URL manually
  const url = client.getUrl(asyncResult.token, 'mp4');
  console.log('Output URL:', url);

  // Poll for progress (only available for async renders)
  let progress = await client.getProgress(asyncResult.token);
  while (progress.done < progress.total) {
    console.log(`Progress: ${progress.done}/${progress.total}`);
    await new Promise((resolve) => setTimeout(resolve, 1000));
    progress = await client.getProgress(asyncResult.token);
  }
  console.log('Render finished!');
}

main().catch(console.error);
