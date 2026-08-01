package com.masl.goofy_protocol_fis_be.service;

import com.masl.goofy_protocol_fis_be.entity.User;
import com.masl.goofy_protocol_fis_be.entity.UserQuotas;
import com.masl.goofy_protocol_fis_be.properties.AdminQuotaProperties;
import com.masl.goofy_protocol_fis_be.properties.BaseQuotaProperties;
import com.masl.goofy_protocol_fis_be.repository.UserQuotasRepository;
import com.masl.goofy_protocol_fis_be.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class QuotaService {
    private final BaseQuotaProperties baseQuotaProperties;
    private final AdminQuotaProperties adminQuotaProperties;
    private final UserRepository userRepository;
    private final UserQuotasRepository userQuotasRepository;

    public QuotaService(BaseQuotaProperties baseQuotaProperties, AdminQuotaProperties adminQuotaProperties, UserRepository userRepository, UserQuotasRepository userQuotasRepository) {
        this.baseQuotaProperties = baseQuotaProperties;
        this.adminQuotaProperties = adminQuotaProperties;
        this.userRepository = userRepository;
        this.userQuotasRepository = userQuotasRepository;
    }

    public BaseQuotaProperties getUserQuotas(String handle) {
        User user = userRepository.findByHandle(handle);

        // Get Quotas
        BaseQuotaProperties baseQuotas = (user == null || !user.isAdmin()) ? baseQuotaProperties : adminQuotaProperties;
        UserQuotas quotas = userQuotasRepository.findByUserHandle(handle);

        return UserQuotas.getAppliedQuotas(quotas, baseQuotas);
    }
}
