package com.beyond.ordersystem.member.controller;

import com.beyond.ordersystem.common.auth.JwtTokenProvider;
import com.beyond.ordersystem.member.domain.Member;
import com.beyond.ordersystem.member.dto.LoginDto;
import com.beyond.ordersystem.member.dto.MemberRefreshDto;
import com.beyond.ordersystem.member.dto.MemberResDto;
import com.beyond.ordersystem.member.dto.MemberSaveReqDto;
import com.beyond.ordersystem.member.service.MemberService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.validation.Valid;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/member")
public class MemberController {
    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;

    @Qualifier("rtdb")
    private final RedisTemplate<String,Object> redisTemplate;
    @Value("${jwt.secretKeyRT}")
    private String secretKeyRT;

    public MemberController(MemberService memberService, JwtTokenProvider jwtTokenProvider, @Qualifier("rtdb") RedisTemplate<String, Object> redisTemplate) {
        this.memberService = memberService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.redisTemplate = redisTemplate;
    }

    @PostMapping("/create")
    public ResponseEntity<?> memberCreate(@Valid @RequestBody MemberSaveReqDto memberSaveReqDto){
        Long memberId = memberService.save(memberSaveReqDto);
        return new ResponseEntity<>(memberId,HttpStatus.CREATED);
    }

    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')") //관리자만 볼수있게끔 하는방법(가장 편한 방법), ROLE_ 붙일필요 없음. 예외는 filter 레벨에서 발생.
    public ResponseEntity<?> memberList(){
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        if(!authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))){
//            throw new AccessDeniedException("권한없음");
//        }
        List<MemberResDto> memberResDto= memberService.findAll();
        return new ResponseEntity<>(memberResDto,HttpStatus.OK);
    }

    @GetMapping("/myinfo")
    public ResponseEntity<?> MyInfo(){
        MemberResDto memberResDto = memberService.myInfo();
        return new ResponseEntity<>(memberResDto,HttpStatus.OK);
    }

    @PostMapping("/doLogin")
    public ResponseEntity<?> doLogin(@RequestBody LoginDto dto){
//        email, password 검증
        Member member = memberService.login(dto);
//        토큰 생성 및 return
        String token = jwtTokenProvider.createToken(member.getEmail() ,member.getRole().toString());
        String refreshToken = jwtTokenProvider.createRefreshToken(member.getEmail() ,member.getRole().toString());
//        redis에 refreshToken 저장
        redisTemplate.opsForValue().set(member.getEmail(),refreshToken, 200, TimeUnit.DAYS); //200일 time to live
        System.out.println(token);
        System.out.println(refreshToken);
//        사용자에게 at, rt지급
        Map<String, Object> loginInfo = new HashMap<>();
        loginInfo.put("id",member.getId());
        loginInfo.put("token",token);
        loginInfo.put("refreshToken",refreshToken);
        return new ResponseEntity<>(loginInfo,HttpStatus.OK);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> generateNewAt(@RequestBody MemberRefreshDto dto){
//        rt디코딩 후 email 추출
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKeyRT)
                .build()
                .parseClaimsJws(dto.getRefreshToken())
                .getBody();
//        rt를 redis의 rt와 비교 검증
        Object rt = redisTemplate.opsForValue().get(claims.getSubject());
        System.out.println("email : "+claims.getSubject());
        System.out.println("rt : "+ rt);
        System.out.println("dto.getRefreshToken() : "+ dto.getRefreshToken());
        if(rt == null || !rt.toString().equals(dto.getRefreshToken())){
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
//        at 생성하여 지급
        String token = jwtTokenProvider.createToken(claims.getSubject(),claims.get("role").toString());
        Map<String, Object> loginInfo = new HashMap<>();
        loginInfo.put("token",token);
        return new ResponseEntity<>(loginInfo,HttpStatus.OK);
    }

}
