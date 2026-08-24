from fastapi import FastAPI, Request
from fastapi.responses import JSONResponse
from fastapi.middleware.cors import CORSMiddleware
from app.routers import recommendation
from app.kafka.consumer import start_consumer
import asyncio
import traceback

app = FastAPI(title="AI Service", version="1.0.0")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:4200"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

@app.middleware("http")
async def catch_exceptions(request: Request, call_next):
    try:
        return await call_next(request)
    except Exception as e:
        traceback.print_exc()
        return JSONResponse(status_code=500, content={"detail": str(e)})

app.include_router(recommendation.router, prefix="/recommendations", tags=["Recommendations"])

@app.on_event("startup")
async def startup_event():
    asyncio.create_task(start_consumer())

@app.get("/health")
def health():
    return {"status": "ok"}