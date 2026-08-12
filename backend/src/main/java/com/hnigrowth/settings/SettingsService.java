package com.hnigrowth.settings;

import com.hnigrowth.audit.AuditAction;
import com.hnigrowth.audit.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SettingsService {

    private final SettingRepository repository;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public Optional<String> get(String key) {
        return repository.findByKey(key).map(Setting::getValue);
    }

    @Transactional(readOnly = true)
    public List<Setting> list() {
        return repository.findAll().stream().map(this::mask).toList();
    }

    @Transactional
    public Setting upsert(String key, String value, boolean secret, String description) {
        Setting s = repository.findByKey(key).orElseGet(() -> Setting.builder().key(key).build());
        s.setValue(value);
        s.setSecret(secret);
        if (description != null) s.setDescription(description);
        Setting saved = repository.save(s);
        auditService.log(AuditAction.UPDATE, "Setting", saved.getId(), "Setting " + key + " updated");
        return mask(saved);
    }

    private Setting mask(Setting s) {
        if (!s.isSecret() || s.getValue() == null || s.getValue().isBlank()) return s;
        Setting copy = Setting.builder().key(s.getKey()).secret(true).description(s.getDescription()).build();
        copy.setId(s.getId());
        String v = s.getValue();
        copy.setValue(v.length() <= 4 ? "****" : "****" + v.substring(v.length() - 4));
        return copy;
    }
}
