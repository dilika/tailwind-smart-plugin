#!/usr/bin/env node

// Version hardcodée puisque nous n'avons plus package.json
const VERSION = '1.0.0';

// Solution alternative à chalk pour la coloration (léger et sans dépendance)
const colors = {
    red: (text) => `\x1b[31m${text}\x1b[0m`,
    yellow: (text) => `\x1b[33m${text}\x1b[0m`,
    blue: (text) => `\x1b[34m${text}\x1b[0m`,
    bold: (text) => `\x1b[1m${text}\x1b[0m`
};

// Gestion des arguments CLI simplifiée
function parseArgs(args) {
    return {
        watch: args.includes('--watch'),
        debug: args.includes('--debug'),
        help: args.includes('--help'),
        version: args.includes('--version'),
        config: getConfigPath(args)
    };
}

function getConfigPath(args) {
    const configIndex = args.indexOf('--config');
    return configIndex !== -1 && args[configIndex + 1] || null;
}

function showHelp() {
    console.log(`
${colors.blue('Usage:')} extractor [options]

${colors.blue('Options:')}
  --watch       Active le mode surveillance
  --debug       Active les logs détaillés
  --config <path> Chemin vers la config
  --version     Affiche la version
  --help        Affiche ce message

${colors.blue('Examples:')}
  $ extractor --watch
  $ extractor --config ./tailwind.config.js
`);
}

async function main() {
    const options = parseArgs(process.argv.slice(2));

    if (options.help) {
        showHelp();
        return;
    }

    if (options.version) {
        console.log(`Version: ${VERSION}`);
        return;
    }

    try {
        // Chargement dynamique pour éviter les dépendances globales
        const { TailwindExtractor } = await import('../lib/TailwindExtractor.js');
        const extractor = new TailwindExtractor(options);

        await extractor.run();

        if (options.watch) {
            console.log(colors.blue('\nMode surveillance actif (Ctrl+C pour quitter)'));
        }
    } catch (err) {
        console.error(colors.red('Erreur:'), err.message);
        process.exit(1);
    }
}

// Gestion des signaux
process.on('SIGINT', () => {
    console.log('\nExtraction terminée');
    process.exit(0);
});

// Point d'entrée
main().catch(e => {
    console.error(colors.red('Erreur non gérée:'), e);
    process.exit(1);
});