# Forum Moderation API

AI-powered content moderation microservice for the [University Forum Platform](https://forum.ieu.app). Built with Java 21, Spring Boot 4, and Google Gemini API.

This microservice analyzes user-generated content in real-time, detecting spam, profanity, and harassment to maintain a safe community environment for 8,000+ active university students.

## Tech Stack

- **Java 21** + **Spring Boot 4**
- **Google Gemini 2.5 Flash** for AI-powered content analysis
- **REST API** with JSON request/response
- **Docker** for containerized deployment
- **Lombok** for clean model code
- **Spring Actuator** for health monitoring

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/moderate/text` | Analyze a single text for moderation |
| `POST` | `/api/moderate/batch` | Analyze multiple texts in bulk |
| `GET` | `/api/moderate/stats` | Get moderation statistics |
| `GET` | `/api/moderate/health` | Health check |

## Quick Start

### Prerequisites
- Java 21+
- [Gemini API Key](https://aistudio.google.com/apikey)

### Run

```bash
GEMINI_API_KEY=your_api_key ./gradlew bootRun
```

### Test

```bash
curl -X POST http://localhost:8080/api/moderate/text \
  -H "Content-Type: application/json" \
  -d '{"text": "Hello, can someone share the exam notes?", "userId": "user1", "postId": "post1"}'
```

### Response

```json
{
  "safe": true,
  "category": "clean",
  "confidence": 0.98,
  "reason": "The user is asking for course notes, which is a legitimate request for a university forum.",
  "postId": "post1"
}
```

## Moderation Categories

| Category | Description |
|----------|-------------|
| `clean` | Content is appropriate |
| `spam` | Spam or advertising |
| `profanity` | Offensive language or hate speech |
| `harassment` | Targeting or bullying other users |

## Docker

```bash
./gradlew build -x test
docker build -t forum-moderation-api .
docker run -p 8080:8080 -e GEMINI_API_KEY=your_key forum-moderation-api
```

## Architecture

```
[NodeBB Forum] → webhook → [Forum Moderation API] → [Gemini AI]
                                    ↓
                            safe/flagged response
```

This microservice is part of the university forum platform ecosystem, designed to work alongside the Node.js-based NodeBB forum as an independent Java microservice following microservices architecture.

## Configuration

| Property | Default | Description |
|----------|---------|-------------|
| `server.port` | `8080` | Server port |
| `gemini.api.key` | - | Gemini API key (via env variable) |
| `gemini.api.url` | `gemini-2.5-flash` | Gemini model endpoint |
| `moderation.confidence-threshold` | `0.7` | Minimum confidence to flag content |
