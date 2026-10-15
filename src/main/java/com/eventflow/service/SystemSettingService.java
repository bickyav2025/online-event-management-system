package com.eventflow.service;

import com.eventflow.entity.SystemSetting;
import com.eventflow.repository.SystemSettingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SystemSettingService {

    private final SystemSettingRepository settingRepository;

    public SystemSettingService(SystemSettingRepository settingRepository) {
        this.settingRepository = settingRepository;
    }

    public List<SystemSetting> getAllSettings() {
        return settingRepository.findAll();
    }

    public Map<String, String> getSettingsMap() {
        Map<String, String> map = new HashMap<>();
        for (SystemSetting s : settingRepository.findAll()) {
            map.put(s.getKey(), s.getValue());
        }
        return map;
    }

    public String getSetting(String key, String defaultValue) {
        return settingRepository.findByKey(key)
                .map(SystemSetting::getValue)
                .orElse(defaultValue);
    }

    @Transactional
    public void updateSettings(Map<String, String> formValues) {
        for (Map.Entry<String, String> entry : formValues.entrySet()) {
            settingRepository.findByKey(entry.getKey()).ifPresent(setting -> {
                setting.setValue(entry.getValue());
                settingRepository.save(setting);
            });
        }
    }
}
