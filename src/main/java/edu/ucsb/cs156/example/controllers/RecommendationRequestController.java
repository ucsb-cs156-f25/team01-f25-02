package edu.ucsb.cs156.example.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import edu.ucsb.cs156.example.entities.RecommendationRequest;
import edu.ucsb.cs156.example.repositories.RecommendationRequestRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** This is a REST controller for RecommendationRequests */
@Tag(name = "RecommendationRequest")
@RequestMapping("/api/recommendationrequests")
@RestController
@Slf4j
public class RecommendationRequestController extends ApiController {

  @Autowired RecommendationRequestRepository recommendationRequestRepository;

  /**
   * List all Recommendation Requests
   *
   * @return an iterable of Recommendation Requests
   */
  @Operation(summary = "List all recommendation requests")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("/all")
  public Iterable<RecommendationRequest> allRecommendationRequests() {
    Iterable<RecommendationRequest> recommendationRequests =
        recommendationRequestRepository.findAll();
    return recommendationRequests;
  }

  /**
   * Create a new recommendation request
   *
   * @param requesterEmail email of the requester
   * @param professorEmail email of the professor
   * @param explanation explanation for the recommendation
   * @param dateRequested date the recommendation was requested
   * @param dateNeeded date the recommendation is needed
   * @param done whether the recommendation is done
   * @return the saved recommendation request
   */
  @Operation(summary = "Create a recommendation request")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PostMapping("/post")
  public RecommendationRequest postRecommendationRequest(
      @Parameter(name = "requesterEmail") @RequestParam String requesterEmail,
      @Parameter(name = "professorEmail") @RequestParam String professorEmail,
      @Parameter(name = "explanation") @RequestParam String explanation,
      @Parameter(name = "dateRequested")
          @RequestParam("dateRequested")
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime dateRequested,
      @Parameter(name = "dateNeeded")
          @RequestParam("dateNeeded")
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime dateNeeded,
      @Parameter(name = "done") @RequestParam boolean done)
      throws JsonProcessingException {

    // For an explanation of @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    // See: https://www.baeldung.com/spring-date-parameters

    log.info("localDateTime={}", dateRequested);

    RecommendationRequest recommendationRequest = new RecommendationRequest();
    recommendationRequest.setRequesterEmail(requesterEmail);
    recommendationRequest.setProfessorEmail(professorEmail);
    recommendationRequest.setExplanation(explanation);
    recommendationRequest.setDateRequested(dateRequested);
    recommendationRequest.setDateNeeded(dateNeeded);
    recommendationRequest.setDone(done);

    RecommendationRequest savedRecommendationRequest =
        recommendationRequestRepository.save(recommendationRequest);

    return savedRecommendationRequest;
  }
}
