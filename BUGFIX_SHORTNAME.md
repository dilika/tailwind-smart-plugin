# 🐛 Correction du Bug ShortName Dupliqué

## Problème

Le plugin générait une erreur au démarrage :
```
Short name 'TailwindInvalidClass' is not unique
```

## Cause

Chaque inspection était enregistrée plusieurs fois dans `plugin.xml` pour différentes langues (HTML, JavaScript, TypeScript, JSX, TSX, Vue), mais elles utilisaient toutes le même `shortName`.

IntelliJ exige que chaque inspection ait un `shortName` **unique**, même si c'est la même classe d'inspection utilisée pour différentes langues.

## Solution

### Avant ❌
```xml
<localInspection language="HTML" shortName="TailwindInvalidClass" .../>
<localInspection language="JavaScript" shortName="TailwindInvalidClass" .../>
<localInspection language="TypeScript" shortName="TailwindInvalidClass" .../>
<!-- etc. - tous avec le même shortName -->
```

### Après ✅
```xml
<localInspection language="HTML" shortName="TailwindInvalidClassHTML" .../>
<localInspection language="JavaScript" shortName="TailwindInvalidClassJS" .../>
<localInspection language="TypeScript" shortName="TailwindInvalidClassTS" .../>
<localInspection language="JSX" shortName="TailwindInvalidClassJSX" .../>
<localInspection language="TSX" shortName="TailwindInvalidClassTSX" .../>
<localInspection language="Vue" shortName="TailwindInvalidClassVue" .../>
```

## Changements

1. **TailwindInvalidClassInspection** : Ajout d'un suffixe de langue au `shortName`
   - `TailwindInvalidClassHTML`
   - `TailwindInvalidClassJS`
   - `TailwindInvalidClassTS`
   - `TailwindInvalidClassJSX`
   - `TailwindInvalidClassTSX`
   - `TailwindInvalidClassVue`

2. **TailwindClassConflictInspection** : Même correction
   - `TailwindClassConflictHTML`
   - `TailwindClassConflictJS`
   - `TailwindClassConflictTS`
   - `TailwindClassConflictJSX`
   - `TailwindClassConflictTSX`
   - `TailwindClassConflictVue`

## Résultat

✅ **Plus d'erreurs** : Le plugin démarre sans erreur
✅ **ShortNames uniques** : Chaque inspection a maintenant un identifiant unique
✅ **Fonctionnalité préservée** : Les inspections fonctionnent toujours pour toutes les langues

## Note

Le `displayName` reste le même pour toutes les langues, ce qui est correct. Seul le `shortName` (identifiant interne) doit être unique.

---

*Bug corrigé le : $(date)*
*Build réussi : ✅*

