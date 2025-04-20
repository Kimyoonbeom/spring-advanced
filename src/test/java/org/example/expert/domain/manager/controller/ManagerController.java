package org.example.expert.domain.manager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.expert.config.AuthUserArgumentResolver;
import org.example.expert.config.JwtUtil;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.manager.dto.request.ManagerSaveRequest;
import org.example.expert.domain.manager.dto.response.ManagerResponse;
import org.example.expert.domain.manager.dto.response.ManagerSaveResponse;
import org.example.expert.domain.manager.service.ManagerService;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ManagerController.class)
class ManagerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ManagerService managerService;

    @MockBean
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void saveManager_성공() throws Exception {
        // given
        long todoId = 1L;
        ManagerSaveRequest request = new ManagerSaveRequest(2L);
        ManagerSaveResponse response = new ManagerSaveResponse(1L, new UserResponse(2L, "test@test.com"));

        given(managerService.saveManager(any(AuthUser.class), eq(todoId), any(ManagerSaveRequest.class)))
                .willReturn(response);

        // 테스트 요청에 AuthUser 속성 추가
        mockMvc.perform(post("/todos/{todoId}/managers", todoId)
                        .requestAttr("userId", 1L) // AuthUserArgumentResolver에서 필요
                        .requestAttr("email", "user@test.com")
                        .requestAttr("userRole", "USER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print()) // 오류 메시지 확인
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void getMembers_성공() throws Exception {
        long todoId = 1L;
        ManagerResponse managerResponse = new ManagerResponse(1L, new UserResponse(2L, "manager@test.com"));
        given(managerService.getManagers(todoId)).willReturn(List.of(managerResponse));

        mockMvc.perform(get("/todos/{todoId}/managers", todoId)
                        .requestAttr("userId", 1L)
                        .requestAttr("email", "user@test.com")
                        .requestAttr("userRole", "USER")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    void deleteManager_성공() throws Exception {
        long todoId = 1L;
        long managerId = 2L;

        mockMvc.perform(delete("/todos/{todoId}/managers/{managerId}", todoId, managerId)
                        .requestAttr("userId", 1L)
                        .requestAttr("email", "user@test.com")
                        .requestAttr("userRole", "USER")
                )
                .andExpect(status().isOk());

        Mockito.verify(managerService).deleteManager(any(AuthUser.class), eq(todoId), eq(managerId));
    }
}
