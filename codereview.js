#!/usr/bin/env node
// codeReview.js
const { execFileSync } = require('child_process');
const minimist = require('minimist');

function runOllama(model, prompt, { quantize = 'q4_0', nGpuLayers = 20 } = {}) {
  // build args for: ollama run MODEL --prompt "<prompt>" --quantize q4_0 --n-gpu-layers 20
  const args = [
    'run', model,
    '--prompt', prompt.replace(/"/g, '\\"'),
    '--quantize', quantize,
    '--n-gpu-layers', String(nGpuLayers)
  ];
  return execFileSync('ollama', args, { encoding: 'utf8' });
}

async function main() {
  const { request, snippets } = minimist(process.argv.slice(2));

  const supervisor = 'qwen3:0.6b';
  const workers    = ['qwen2.5-coder:7b', 'yicoder:9b', 'deepseek-r1:8b'];
  const voter      = 'qwen3:4b';

  // Phase 1: Plan & Summarize on GPU
  const plan = runOllama(supervisor, `
CONTEXT: ${snippets}
REQUEST: ${request}
OUTPUT: 3–5 step plan
`, { nGpuLayers: 20 });

  const summary = runOllama(supervisor, `
PLAN: ${plan}
OUTPUT: one-sentence summary
`, { nGpuLayers: 20 });

  // Phase 2: Sequential Proposals (unload automatically when each run ends)
  const proposals = workers.map(model =>
    runOllama(model, `
PLAN: ${plan}
SUMMARY: ${summary}
SNIPPETS: ${snippets}
OUTPUT: code patch + rationale
`, { nGpuLayers: 20 })
  );

  // Phase 3: Voting Round on CPU
  const combined = proposals.join('\n\n---\n\n');
  const voteOut = runOllama(voter, `
PROPOSALS:
${combined}
OUTPUT: rank & pick best patch
`, { nGpuLayers: 0 });

  // The voter model’s output is our “best patch”
  console.log(voteOut.trim());
}

main().catch(err => {
  console.error(err);
  process.exit(1);
});