from pydantic import BaseModel
from typing import List, Optional

class MediaItem(BaseModel):
    id: str
    title: str
    genre: Optional[str]
    type: Optional[str]
    tags: Optional[List[str]] = []

class RecommendationResponse(BaseModel):
    userId: str
    recommendations: List[MediaItem]