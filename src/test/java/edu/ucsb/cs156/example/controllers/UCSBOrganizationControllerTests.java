package edu.ucsb.cs156.example.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.UCSBOrganization;
import edu.ucsb.cs156.example.repositories.UCSBOrganizationRepository;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(controllers = UCSBOrganizationController.class)
@Import(TestConfig.class)
public class UCSBOrganizationControllerTests extends ControllerTestCase {

  @MockBean UCSBOrganizationRepository ucsbOrganizationRepository;

  @MockBean UserRepository userRepository;

  // Authorization tests for /api/ucsborganization/all

  @Test
  public void logged_out_users_cannot_get_all() throws Exception {
    mockMvc
        .perform(get("/api/ucsborganization/all"))
        .andExpect(status().is(403)); // logged out users can't get all
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_can_get_all() throws Exception {
    mockMvc.perform(get("/api/ucsborganization/all")).andExpect(status().is(200)); // logged
  }

  // Authorization tests for /api/ucsborganization/post
  // (Perhaps should also have these for put and delete)

  @Test
  public void logged_out_users_cannot_post() throws Exception {
    mockMvc.perform(post("/api/ucsborganization/post")).andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_regular_users_cannot_post() throws Exception {
    mockMvc
        .perform(post("/api/ucsborganization/post"))
        .andExpect(status().is(403)); // only admins can post
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_can_get_all_ucsborganizations() throws Exception {

    // arrange

    UCSBOrganization zpr =
        UCSBOrganization.builder()
            .orgCode("ZPR")
            .orgTranslationShort("ZETA PHI RHO")
            .orgTranslation("ZETA PHI RHO")
            .inactive(false)
            .build();

    ArrayList<UCSBOrganization> expectedOrganizations = new ArrayList<>();
    expectedOrganizations.addAll(Arrays.asList(zpr));

    when(ucsbOrganizationRepository.findAll()).thenReturn(expectedOrganizations);

    // act
    MvcResult response =
        mockMvc.perform(get("/api/ucsborganization/all")).andExpect(status().isOk()).andReturn();

    // assert

    verify(ucsbOrganizationRepository, times(1)).findAll();
    String expectedJson = mapper.writeValueAsString(expectedOrganizations);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void an_admin_user_can_post_a_new_organization() throws Exception {
    // arrange

    UCSBOrganization zpr =
        UCSBOrganization.builder()
            .orgCode("ZPR")
            .orgTranslationShort("ZETA_PHI_RHO")
            .orgTranslation("ZETA_PHI_RHO")
            .inactive(true)
            .build();

    when(ucsbOrganizationRepository.save(eq(zpr))).thenReturn(zpr);

    // act
    MvcResult response =
        mockMvc
            .perform(
                post("/api/ucsborganization/post?orgCode=ZPR&orgTranslationShort=ZETA_PHI_RHO&orgTranslation=ZETA_PHI_RHO&inactive=true")
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(ucsbOrganizationRepository, times(1)).save(zpr);
    String expectedJson = mapper.writeValueAsString(zpr);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);

    // Additional test for inactive field
    UCSBOrganization savedOrganization = mapper.readValue(responseString, UCSBOrganization.class);
    assertEquals(true, savedOrganization.getInactive());
  }

  @Test
  public void logged_out_users_cannot_get_by_id() throws Exception {
    mockMvc
        .perform(get("/api/ucsborganization?orgCode=ZPR"))
        .andExpect(status().is(403)); // logged out users can't get by id
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

    // arrange

    when(ucsbOrganizationRepository.findById(eq("KRC"))).thenReturn(Optional.empty());

    // act
    MvcResult response =
        mockMvc
            .perform(get("/api/ucsborganization?orgCode=KRC"))
            .andExpect(status().isNotFound())
            .andReturn();

    // assert

    verify(ucsbOrganizationRepository, times(1)).findById(eq("KRC"));
    Map<String, Object> json = responseToJson(response);
    assertEquals("EntityNotFoundException", json.get("type"));
    assertEquals("UCSBOrganization with id KRC not found", json.get("message"));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

    // arrange

    UCSBOrganization zpr =
        UCSBOrganization.builder()
            .orgCode("ZPR")
            .orgTranslationShort("ZETA_PHI_RHO")
            .orgTranslation("ZETA_PHI_RHO")
            .inactive(true)
            .build();

    when(ucsbOrganizationRepository.findById(eq("ZPR"))).thenReturn(Optional.of(zpr));

    // act
    MvcResult response =
        mockMvc
            .perform(get("/api/ucsborganization?orgCode=ZPR"))
            .andExpect(status().isOk())
            .andReturn();

    // assert

    verify(ucsbOrganizationRepository, times(1)).findById(eq("ZPR"));
    String expectedJson = mapper.writeValueAsString(zpr);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_edit_an_existing_organization() throws Exception {
    // arrange

    UCSBOrganization skyOrig =
        UCSBOrganization.builder()
            .orgCode("SKY")
            .orgTranslationShort("SKYDIVING_CLUB")
            .orgTranslation("SKYDIVING_CLUB_AT_UCSB")
            .inactive(false)
            .build();

    UCSBOrganization skyEdited =
        UCSBOrganization.builder()
            .orgCode("SKY")
            .orgTranslationShort("SKY_DIVING_CLUB")
            .orgTranslation("SKY_DIVING_CLUB_AT_UCSB")
            .inactive(true)
            .build();

    String requestBody = mapper.writeValueAsString(skyEdited);

    when(ucsbOrganizationRepository.findById(eq("SKY"))).thenReturn(Optional.of(skyOrig));

    // act
    MvcResult response =
        mockMvc
            .perform(
                put("/api/ucsborganization?orgCode=SKY")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("utf-8")
                    .content(requestBody)
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(ucsbOrganizationRepository, times(1)).findById("SKY");
    verify(ucsbOrganizationRepository, times(1))
        .save(skyEdited); // should be saved with updated info
    String responseString = response.getResponse().getContentAsString();
    assertEquals(requestBody, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_cannot_edit_organization_that_does_not_exist() throws Exception {
    // arrange

    UCSBOrganization editedOrganization =
        UCSBOrganization.builder()
            .orgCode("KRC")
            .orgTranslationShort("KOREAN_RADIO_CL")
            .orgTranslation("KOREAN_RADIO_CLUB_AT_UCSB")
            .inactive(false)
            .build();

    String requestBody = mapper.writeValueAsString(editedOrganization);

    when(ucsbOrganizationRepository.findById(eq("KRC"))).thenReturn(Optional.empty());

    // act
    MvcResult response =
        mockMvc
            .perform(
                put("/api/ucsborganization?orgCode=KRC")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("utf-8")
                    .content(requestBody)
                    .with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    // assert
    verify(ucsbOrganizationRepository, times(1)).findById("KRC");
    Map<String, Object> json = responseToJson(response);
    assertEquals("UCSBOrganization with id KRC not found", json.get("message"));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_delete_an_organization() throws Exception {
    // arrange

    UCSBOrganization sky =
        UCSBOrganization.builder()
            .orgCode("SKY")
            .orgTranslationShort("SKYDIVING_CLUB")
            .orgTranslation("SKYDIVING_CLUB_AT_UCSB")
            .inactive(false)
            .build();

    when(ucsbOrganizationRepository.findById(eq("SKY"))).thenReturn(Optional.of(sky));

    // act
    MvcResult response =
        mockMvc
            .perform(delete("/api/ucsborganization?orgCode=SKY").with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(ucsbOrganizationRepository, times(1)).findById("SKY");
    verify(ucsbOrganizationRepository, times(1)).delete(any());

    Map<String, Object> json = responseToJson(response);
    assertEquals("UCSBOrganization with id SKY deleted", json.get("message"));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_tries_to_delete_non_existant_organization_and_gets_right_error_message()
      throws Exception {
    // arrange

    when(ucsbOrganizationRepository.findById(eq("KRC"))).thenReturn(Optional.empty());

    // act
    MvcResult response =
        mockMvc
            .perform(delete("/api/ucsborganization?orgCode=KRC").with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    // assert
    verify(ucsbOrganizationRepository, times(1)).findById("KRC");
    Map<String, Object> json = responseToJson(response);
    assertEquals("UCSBOrganization with id KRC not found", json.get("message"));
  }
}
