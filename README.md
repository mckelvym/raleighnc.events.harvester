# Raleigh Parks and Recreation News Harvester

This Java application scrapes news from [Raleigh Parks and Recreation](https://raleighnc.gov/news?department=All&service=41) and generates an RSS feed. It uses Selenium WebDriver with headless Chrome to handle JavaScript-rendered content and JSoup for HTML parsing. The application produces an incremental RSS feed that appends new news items to an existing feed file while filtering out old entries.

Feed exported to https://github.com/mckelvym/raleighnc.events.rss

## Build and Run

Build the application:

```bash
./gradlew build
```

Build the image:

```bash
source scripts/version.sh && ./gradlew jib -Djib.to.image=$IMAGE:$VERSION
```

Run with default output file (events.xml):

```bash
./gradlew run
```

Run with custom output file:

```bash
./gradlew run -Pargs='my-events.xml'
```

## Docker

The project uses Jib for containerization. Run with Docker:

```bash
./scripts/run.sh
```

This pulls and runs the latest Docker image from GitHub Container Registry.

## How It Works

The application follows a three-phase workflow:

1. Load Existing Feed - Reads the existing RSS file and extracts all GUIDs to avoid duplicates
2. Scrape News - Uses Selenium to load the first news listing page and builds each item from its teaser (title, date, link, and image). Article pages are not loaded
3. Generate RSS Feed - Creates a new RSS 2.0 XML document with new news items, imports existing items from the old feed, filters out items older than 7 days, and writes the result to the output file

raleighnc.gov is behind Cloudflare. From the production server, later listing pages and every article page get a bot challenge that never clears, while the first listing page loads normally. That page holds the newest 12 items (several days of news), and the job runs several times a day, so nothing is missed. Teasers have no summary text, so items have no description.

## Architecture

The application uses a modular SOLID design with clear separation of concerns:

- Domain layer: EventItem for news data
- Config layer: Site-specific configuration
- WebDriver layer: Chrome automation and page loading
- Scraper layer: Listing-page teaser discovery
- Parser layer: Multi-strategy field extraction
- Feed layer: XXE-protected RSS generation

## Configuration

Event retention period: 7 days
Page load timeout: 10 seconds
Pages to fetch: 1
Bot challenge timeout: 30 seconds
Request delay: 1-3 seconds (randomized)
Target URL: https://raleighnc.gov/news?department=All&service=41

## Output

The generated RSS feed includes:

- News title
- News link (also used as GUID)
- News image (as enclosure, when available)
- Publication date

The feed uses standard RSS 2.0 format with UTF-8 encoding.
