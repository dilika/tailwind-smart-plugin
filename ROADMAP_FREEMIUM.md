# Roadmap Freemium – Tailwind Smart Plugin

*Dernière mise à jour : 2025-05-25*

Ce roadmap détaille les axes d’innovation et les fonctionnalités à développer pour transformer le plugin Tailwind Smart en produit freemium à fort potentiel.

---

## 1. Fonctionnalités Existantes (Base gratuite)

- **Complétion intelligente Tailwind**
  - Support des variantes, classes JIT arbitraires (`[width:250px]`, etc.)
  - Suggestions contextuelles basées sur le préfixe
  - Prise en charge de Tailwind v4 et des utilitaires récents
  - Suggestions enrichies avec icônes colorées et catégories
- **Documentation enrichie**
  - Affichage prioritaire de la documentation améliorée (exemples, équivalents CSS)
  - Support du survol avec affichage contextuel
- **Gestion du fichier `tailwind.config.js`**
  - Analyse dynamique, prise en charge des plugins/extensions personnalisés
- **Folding et navigation avancée**
  - Pliage/dépliage global des classes Tailwind (raccourcis dédiés)
  - Menu contextuel pour opérations Tailwind
  - Tri de toutes les classes Tailwind dans un fichier
- **Support multi-langages**
  - Détection intelligente des attributs `class`/`className` (JSX, TSX, HTML, etc.)
- **Qualité & robustesse**
  - Gestion des erreurs, compatibilité JetBrains, codebase modulaire

---

## 2. Fonctionnalités Premium Proposées (Freemium)

### 2.1. Productivité & Automatisation
- **Refactoring automatique Tailwind**  
  > Suggestion et application automatique des optimisations de classes (fusion, suppression des doublons, conversion vers des groupes ou des shortcuts)
- **Détection et correction des mauvaises pratiques**  
  > Audit intelligent du code Tailwind avec suggestions de correction et auto-fix
- **Recherche avancée de classes**  
  > Recherche multi-fichiers, surlignage dynamique, navigation directe vers l’occurrence

### 2.2. Expérience Utilisateur Avancée
- **Aperçu visuel instantané**  
  > Affichage en temps réel du rendu CSS/HTML pour une classe ou un ensemble de classes (mini-preview dans l’éditeur)
- **Palette interactive Tailwind**  
  > Sélecteur graphique de couleurs, tailles, espaces, etc. avec insertion directe
- **Historique et annulation contextuelle**  
  > Suivi des modifications Tailwind avec rollback ciblé

### 2.3. Collaboration & Personnalisation
- **Partage de snippets Tailwind**  
  > Export/import de collections de classes, partage via cloud ou QR code
- **Synchronisation des presets entre projets**  
  > Sauvegarde/restauration de configurations Tailwind personnalisées
- **Statistiques d’utilisation Tailwind**  
  > Dashboard d’analyse des classes les plus utilisées, heatmap, recommandations

### 2.4. Intégrations et Extensions
- **Export de config Figma → Tailwind**  
  > Permet d’exporter directement les styles, palettes de couleurs, typographies et espacements définis dans Figma vers une configuration `tailwind.config.js` compatible. Idéal pour harmoniser design system et code, gagner du temps et éviter les erreurs manuelles. Fonctionnalité premium : accessible via un panneau dédié dans le plugin, avec mapping automatique ou personnalisé des styles Figma vers Tailwind.
- **Intégration Figma/Design Tools**  
  > Import direct de styles Figma en classes Tailwind
- **Support des frameworks avancés**  
  > Détection automatique de contextes Nuxt, Next, Astro, etc., et adaptation des suggestions

---

## 3. Stratégie Freemium & Différenciation

- **Gratuit** :
  - Complétion, documentation enrichie, folding, tri, menu contextuel, support multi-langages
- **Premium** :
  - Refactoring automatique, audit intelligent, preview visuel, palette interactive, partage cloud, statistiques, intégrations design avancées
- **Activation Premium** :
  - Essai gratuit 7/14 jours, licence JetBrains Marketplace, ou via clé d’activation

---

## 4. Lots de Développement & Priorisation

### Lot 1 : MVP Premium (Core)
- Refactoring automatique
- Audit intelligent
- Aperçu visuel instantané
- Palette interactive

### Lot 2 : Collaboration & Statistiques
- Partage de snippets
- Statistiques d’utilisation
- Synchronisation presets

### Lot 3 : Intégrations avancées
- Figma/Design Tools
- Support frameworks Nuxt/Next/Astro

---

## 5. UX/UI & Monétisation

- Ajout d’un panneau dédié « Pro » dans l’UI
- Indicateurs visuels des features premium (badge, lock, tooltip)
- Onboarding avec tutoriel interactif
- Page de gestion de licence intégrée

---

## 6. Suivi & Feedback

- Système de feedback utilisateur intégré (popup ou menu)
- Collecte anonyme (opt-in) des usages pour améliorer le produit

---

## 7. Références & Inspirations

- Tailwind Labs, Headwind, Windy, JetBrains Marketplace, Figma Plugins

---

**Ce roadmap est évolutif : n’hésite pas à l’enrichir au fil des retours utilisateurs et des tendances du marché !**
