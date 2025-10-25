package com.we.hirehub.controller;

import com.we.hirehub.dto.CompanyDto;
import com.we.hirehub.entity.Company;
import com.we.hirehub.repository.CompanyRepository;
import com.we.hirehub.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
public class CompanyRestController {

    private final CompanyRepository companyRepository;
    private final CompanyService companyService; // Service로 바꿔서 처리

    @PostMapping
    public Company addCompany(@RequestBody CompanyDto companyDto) {
        Company company = Company.builder()
                .name(companyDto.getName())
                .content(companyDto.getContent())
                .address(companyDto.getAddress())
                .since(companyDto.getSince())
                .benefits(companyDto.getBenefits())
                .website(companyDto.getWebsite())
                .industry(companyDto.getIndustry())
                .ceo(companyDto.getCeo())
                .photo(companyDto.getPhoto())
                .build();

        return companyRepository.save(company);
    }

    // 전체 조회
    @GetMapping
    public List<CompanyDto> getAllCompanies() {
        return companyService.getAllCompanies();
    }

    // 단건 조회 (id 기준)
    @GetMapping("/{id}")
    public CompanyDto getCompanyById(@PathVariable Long id) {
        return companyService.getAllCompanies().stream()
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .orElse(null); // 없으면 null 반환, 필요하면 예외 처리 가능
    }
}
