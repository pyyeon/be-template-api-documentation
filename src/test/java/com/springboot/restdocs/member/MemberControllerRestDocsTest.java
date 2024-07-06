package com.springboot.restdocs.member;

import com.google.gson.Gson;
import com.springboot.member.controller.MemberController;
import com.springboot.member.dto.MemberDto;
import com.springboot.member.entity.Member;
import com.springboot.member.mapper.MemberMapper;
import com.springboot.member.service.MemberService;
import com.springboot.stamp.Stamp;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static com.springboot.util.ApiDocumentUtils.getRequestPreProcessor;
import static com.springboot.util.ApiDocumentUtils.getResponsePreProcessor;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.patch;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MemberController.class)
@MockBean(JpaMetamodelMappingContext.class)
@AutoConfigureRestDocs
public class MemberControllerRestDocsTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    //테스트대상이 의존하는 클래스를 작성
    private MemberService memberService;
    @MockBean
    private MemberMapper memberMapper;
    @Autowired
    private Gson gson;

    @Test
    public void postMember() throws Exception {
        //given
        //테스트를 위한 사전 데이터 작업

        MemberDto.Post postDto = new MemberDto.Post(
                "lucky@cat.house", "김러키", "010-1234-5678");

        String content = gson.toJson(postDto);
        //Mock객체를 이용한 Stubbing(given을 통한 메서드 지정)
        given(memberMapper.memberPostToMember(Mockito.any(MemberDto.Post.class))).willReturn(new Member());

        Member resultMember = new Member();
        resultMember.setMemberId(1L);
        given(memberService.createMember(Mockito.any(Member.class)))
                .willReturn(resultMember);
        //when
        ResultActions actions = mockMvc.perform(
                //Request전송
                post("/v11/members")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content)
        );
        //then
        actions.andExpect(status().isCreated())
                .andExpect(header().string("Location", is(startsWith("/v11/members"))))
                .andDo(document(
                        "post-member",
                        getRequestPreProcessor(),
                        getResponsePreProcessor(),
                        requestFields(
                                List.of(
                                        fieldWithPath("email").type(JsonFieldType.STRING).description("이메일"),
                                        fieldWithPath("name").type(JsonFieldType.STRING).description("이름"),
                                        fieldWithPath("phone").type(JsonFieldType.STRING).description("휴대폰 번호")

                                )
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.LOCATION).description("Loction header.등록된 리소스의 URI")
                        )
                ));

        //기대값 검증

    }

    @Test
    public void patchMemberTest() throws Exception {
        //given
        long memberId = 1L;

        //when
        MemberDto.Patch patch = new MemberDto.Patch(memberId,
                "김러키",
                "010-1234-5678",
                Member.MemberStatus.MEMBER_SLEEP);
        String content = gson.toJson(patch);

        MemberDto.Response response = new MemberDto.Response(
                1L,
                "lucky@cat.house",
                "김러키",
                "010-1234-5678",
                Member.MemberStatus.MEMBER_SLEEP,
                new Stamp()
        );
        given(memberMapper.memberPatchToMember(Mockito.any(MemberDto.Patch.class))).willReturn(new Member());
        given(memberService.updateMember(Mockito.any(Member.class))).willReturn(new Member());
        given(memberMapper.memberToMemberResponse(Mockito.any(Member.class))).willReturn(response);

        //then
        ResultActions actions = mockMvc.perform(
                patch("/v11/members/{member-id}", memberId)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content)
        );
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.memberId").value(patch.getMemberId()))
                .andExpect(jsonPath("$.data.name").value(patch.getName()))
                .andExpect(jsonPath("$.data.phone").value(patch.getPhone()))
                .andExpect(jsonPath("$.data.memberStatus").value(patch.getMemberStatus().getStatus()))
                .andDo(document(
                        "patch-member",
                        getRequestPreProcessor(),
                        getResponsePreProcessor(),
                        pathParameters(
                                parameterWithName("member-id").description("회원 식별자")
                        ),
                        requestFields(
                                List.of(
                                        fieldWithPath("memberId").type(JsonFieldType.NUMBER).description("회원 식별자").ignored(),
                                        fieldWithPath("name").type(JsonFieldType.STRING).description("이름").optional(),
                                        fieldWithPath("phone").type(JsonFieldType.STRING).description("휴대폰 번호").optional(),
                                        fieldWithPath("memberStatus").type(JsonFieldType.STRING).description("회원 상태: MEMBER_ACTIVE / MEMBER_SLEEP / MEMBER_QUIT").optional()

                                )
                        ),
                        responseFields(
                                List.of(
                                        fieldWithPath("data").type(JsonFieldType.OBJECT).description("결과 데이터"),
                                        fieldWithPath("data.memberId").type(JsonFieldType.NUMBER).description("회원 식별자"),
                                        fieldWithPath("data.email").type(JsonFieldType.STRING).description("이메일"),
                                        fieldWithPath("data.name").type(JsonFieldType.STRING).description("이름"),
                                        fieldWithPath("data.phone").type(JsonFieldType.STRING).description("휴대폰 번호"),
                                        fieldWithPath("data.memberStatus").type(JsonFieldType.STRING).description("회원 상태: MEMBER_ACTIVE / MEMBER_SLEEP / MEMBER_QUIT"),
                                        fieldWithPath("data.stamp").type(JsonFieldType.NUMBER).description("스탬프 갯수")

                                )
                        )
                ));
    }


}
