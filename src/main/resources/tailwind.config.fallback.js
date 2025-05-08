// Fallback Tailwind config to generate all utility classes via safelist
module.exports = {
  mode: 'jit',
  content: [], // Content overridden via CLI --content
  safelist: [
    {
      pattern: /.*/,
    },
  ],
  theme: {
    extend: {},
  },
  plugins: [],
};
