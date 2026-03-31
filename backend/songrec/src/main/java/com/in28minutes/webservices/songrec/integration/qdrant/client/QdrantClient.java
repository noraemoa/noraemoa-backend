package com.in28minutes.webservices.songrec.integration.qdrant.client;

import com.in28minutes.webservices.songrec.integration.qdrant.config.QdrantProperties;
import com.in28minutes.webservices.songrec.integration.qdrant.dto.QdrantCreateCollectionRequest;
import com.in28minutes.webservices.songrec.integration.qdrant.dto.QdrantCreateCollectionRequest.Vectors;
import com.in28minutes.webservices.songrec.integration.qdrant.dto.QdrantPoint;
import com.in28minutes.webservices.songrec.integration.qdrant.dto.QdrantSearchRequest;
import com.in28minutes.webservices.songrec.integration.qdrant.dto.QdrantSearchResponse;
import com.in28minutes.webservices.songrec.integration.qdrant.dto.QdrantUpsertPointsRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Component
@RequiredArgsConstructor
public class QdrantClient {
  private final WebClient qdrantWebClient;
  private final QdrantProperties qdrantProperties;

  public boolean collectionExists(String collectionName) {
    try {
      qdrantWebClient.get()
          .uri("/collections/{collectionName}",collectionName)
          .retrieve()
          .bodyToMono(String.class)
          .block();
      return true;
    }catch(WebClientResponseException e) {
      if(e.getStatusCode().value()==404){
        return false;
      }
      throw e;
    }
  }

  public String createCollection(String collectionName){
    Vectors vectors=Vectors.builder()
        .size(qdrantProperties.getVectorSize())
        .distance(qdrantProperties.getDistance()).build();

    QdrantCreateCollectionRequest request=
        QdrantCreateCollectionRequest.builder()
            .vectors(vectors).build();

    return qdrantWebClient.put()
        .uri("/collections/{collectionName}",collectionName)
        .bodyValue(request)
        .retrieve()
        .onStatus(HttpStatusCode::isError,
            response->response.bodyToMono(String.class)
                .map(body->new RuntimeException("Qdrant create collection failed: "+body)))
        .bodyToMono(String.class)
        .block();
  }

  public void createCollectionIfNotExist(String collectionName){
    if(!collectionExists(collectionName)){
      createCollection(collectionName);
    }
  }

  public String upsertPoints(String collectionName, List<QdrantPoint> points){
    QdrantUpsertPointsRequest request = new QdrantUpsertPointsRequest(points);

    return qdrantWebClient.put()
        .uri("/collections/{collectionName}/points",collectionName)
        .bodyValue(request)
        .retrieve()
        .onStatus(HttpStatusCode::isError,
            response-> response.bodyToMono(String.class)
                .map(body->new RuntimeException("Qdrant upsert failed:"+body)))
        .bodyToMono(String.class)
        .block();
  }

  public String upsertSongPoint(QdrantPoint point){
    return upsertPoints(qdrantProperties.getCollectionName(),List.of(point));
  }

  public QdrantSearchResponse searchSong(List<Float>vector,int limit){
    QdrantSearchRequest request = QdrantSearchRequest.builder()
        .query(vector)
        .limit(limit)
        .with_payload(true)
        .with_vector(false).build();

    return qdrantWebClient.post()
        .uri("/collections/{collectionName}/points/query",qdrantProperties.getCollectionName())
        .bodyValue(request)
        .retrieve()
        .bodyToMono(QdrantSearchResponse.class)
        .block();
  }
}
