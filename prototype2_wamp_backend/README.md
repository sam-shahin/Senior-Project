# WAMP Backend Spike

## Spike Question

Can the system receive a user's legal-information request, search a local database, and return a sourced response through a web backend?

## Purpose

This spike tests a simple local backend for the Let's Talk application. It uses WAMP to run Apache, PHP, and a local MySQL or MariaDB database.

The database stores legal questions, answers, sources, jurisdiction information, and the date the information was last updated.

## Technology Used

- WAMP local web server
- Apache for serving the PHP files
- PHP for backend processing
- MySQL or MariaDB for storing legal information
- JavaScript Fetch API for the AJAX request
- JSON for communication between the webpage and PHP

## Request Flow

```text
User enters a search
        ↓
JavaScript sends an AJAX request
        ↓
PHP receives the search question
        ↓
PHP searches the database
        ↓
The database returns matching legal information
        ↓
PHP sends the result as JSON
        ↓
The webpage displays the answer and source