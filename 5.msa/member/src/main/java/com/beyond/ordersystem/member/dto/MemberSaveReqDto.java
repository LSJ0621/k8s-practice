package com.beyond.ordersystem.member.dto;

import com.beyond.ordersystem.member.domain.Member;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor //기본생성자
@AllArgsConstructor //모든 매개변수있는 생성자
@Data
@Builder
public class MemberSaveReqDto {
    @NotEmpty
    private String name;
    @NotEmpty
    private String email;
    @NotEmpty
    private String password;

    public Member toEntity(String encodedPassword) {
        return Member.builder().name(this.name).email(this.email).password(encodedPassword).build();
    }
}
