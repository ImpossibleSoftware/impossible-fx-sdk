import { ImpossibleFX } from '../src/index.js';

async function main() {
  const client = new ImpossibleFX({
    region: 'us-east-1',
    apiKey: process.env.IMPOSSIBLE_API_KEY,
  });

  // Render a movie with parameters
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

  // Alternatively, create a token first and poll for progress
  const tokenResult = await client.createToken('my-project', 'intro-video', {
    title: 'Async Render',
  });

  console.log('\nToken created:', tokenResult.token);

  // Build the URL manually
  const url = client.getUrl(tokenResult.token, 'mp4');
  console.log('Output URL:', url);

  // Poll for progress
  let progress = await client.getProgress(tokenResult.token);
  while (progress.done < progress.total) {
    console.log(`Progress: ${progress.done}/${progress.total}`);
    await new Promise((resolve) => setTimeout(resolve, 1000));
    progress = await client.getProgress(tokenResult.token);
  }
  console.log('Render finished!');
}

main().catch(console.error);
