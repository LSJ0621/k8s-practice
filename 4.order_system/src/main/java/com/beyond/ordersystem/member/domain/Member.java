package com.beyond.ordersystem.member.domain;

import com.beyond.ordersystem.common.domain.BaseTimeEntity;
import com.beyond.ordersystem.member.dto.MemberResDto;
import com.beyond.ordersystem.ordering.domain.Ordering;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
@Entity
@Builder
public class Member extends BaseTimeEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @Column(unique = true,nullable = false)
    private String email;
    private String password;
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Role role =Role.USER;
    @OneToMany(mappedBy = "member")
    private List<Ordering> orderingList;

    public MemberResDto fromEntity(){
        int orderCount = this.orderingList.size();
        return MemberResDto.builder().id(this.id).name(this.name).email(this.email).orderCount(orderCount).build();
    }
}
