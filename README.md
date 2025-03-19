# DOJO Architecture hexagonale

## Introduction

Vous avez sûrement déjà entendu parler de l'architecture hexagonale, aussi appelée architecture ports et adaptateurs. Cette architecture est une manière de structurer votre code pour le rendre plus modulaire, plus testable et plus maintenable. Elle est particulièrement adaptée aux applications métier.

## Objectifs

Dans ce dojo, nous allons voir comment mettre en place une architecture hexagonale dans une application Java existante. Nous allons voir comment découper notre application en différents modules, comment les relier entre eux et comment les tester. Nous allons également voir comment tester notre architecture et s'assurer qu'aucune couche n'est polluée en mettant en place des test ArchUnit.

## Prérequis

- Java 21
- Maven
- Un IDE (IntelliJ IDEA, Eclipse, VS Code, etc.)
- Git
- Docker ou Podman

## Quelques notions

### Principes SOLID

Les principes SOLID sont fondamentaux pour comprendre pourquoi une architecture hexagonale peut être utile :

- Single Responsibility Principle : Un composant doit avoir une seule raison de changer, c'est à dire une seule responsabilité.
- Open/Closed Principle : Un composant doit être ouvert à l'extension mais fermé à la modification, c'est à dire qu'on doit pouvoir ajouter des fonctionnalités sans modifier le code existant.
- Liskov Substitution Principle : Les objets d'une classe dérivée doivent pouvoir remplacer les objets de la classe de base sans affecter le comportement du programme, c'est à dire que les sous-classes doivent être substituables à la classe de base.
- Interface Segregation Principle : Les interfaces doivent être spécifiques aux besoins des clients, c'est à dire qu'on ne doit pas forcer les clients à implémenter des méthodes dont ils n'ont pas besoin.
- Dependency Inversion Principle : Les modules de haut niveau ne doivent pas dépendre des modules de bas niveau. Les deux doivent dépendre d'abstractions. Les détails doivent dépendre des abstractions.

Ces principes et notamment le plus crucial, le principe d'inversion de dépendance, sont au coeur de l'architecture hexagonale.

### Domain-Driven Design (DDD)

L'architecture hexagonale prend souvent racine dans les concepts de DDD.

Domaine Métier : Le cœur de l'application est la logique métier.
Ubiquitous Language : Un langage commun entre les développeurs et les experts métier.
Entities et Value Objects : Des objets qui représentent les concepts fondamentaux du métier.

![DDD](assets/DDD.jpg)

### Inversion de Contrôle (IoC)

L'inversion de contrôle est un principe qui consiste à déléguer la gestion des dépendances à un conteneur IoC. Cela permet de rendre les composants indépendants des détails de leur création et de leur configuration.

Dependency Injection est une technique pour implémenter l'IoC. Les dépendances sont injectées dans les composants au lieu d'être créées par eux.

![Injection de Dépendance](assets/dep-inj.webp)

### Séparation des Préoccupations

Le but de l'architecture hexagonale est de séparer la logique métier des détails techniques (UI, bases de données, frameworks). Le cœur de l'application doit être indépendant des technologies et se concentrer uniquement sur le métier.

![Séparation](assets/separation.png)

### Ports et Adaptateurs (Hexagone)

Ce sont les interfaces qui définissent les moyens par lesquels l'application interagit avec le monde extérieur, comme les interfaces utilisateur ou les appels à des API externes.

![Clean Architecture](./assets/hexagonal_architecture.png)

### Indépendance du Domaine

Le domaine métier doit être complètement indépendant des technologies utilisées (par exemple, la base de données ou le framework web). Les dépendances sont inversées (par exemple, la base de données dépend de la logique métier, et non l'inverse).

### Facilité d'Extension

L'un des grands avantages de l'architecture hexagonale est la possibilité de changer les adaptateurs sans toucher au cœur de l'application. Par exemple, remplacer un système de persistance relationnel par une solution NoSQL est possible en changeant uniquement l'adaptateur correspondant.

### Testabilité et Flexibilité

Étant donné que le domaine métier est indépendant, il devient facile de tester la logique métier avec des mockups ou des stubs pour les interactions externes.

### Synthèse

L'architecture hexagonale est une approche modulaire et flexible qui améliore la testabilité, la maintenabilité et l'évolutivité.

Elle est adaptée aux systèmes complexes, pas nécessaire pour les petits projets..

## Mise en place

### Structure de l'application

