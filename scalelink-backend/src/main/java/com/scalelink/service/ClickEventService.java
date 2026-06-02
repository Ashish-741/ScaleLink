package com.scalelink.service;

import com.scalelink.entity.ClickEvent;
import com.scalelink.entity.Url;
import com.scalelink.repository.ClickEventRepository;
import com.scalelink.repository.UrlRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Click Event Service
 * Handles analytics tracking asynchronously.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ClickEventService {

    private final ClickEventRepository clickEventRepository;
    private final UrlRepository urlRepository;

    /**
     * Record a click event.
     * 
     * WHY @Async?
     * We don't want the user's redirect to be delayed by database writes!
     * By marking this @Async, Spring runs this method in a separate thread pool
     * (the one we configured in AsyncConfig). The redirect happens instantly,
     * and this tracking happens quietly in the background.
     */
    @Async
    @Transactional
    public void recordClick(Url url, String ipAddress, String userAgent, String referer) {
        log.debug("Recording click for URL ID: {} asynchronously", url.getId());
        
        ClickEvent clickEvent = ClickEvent.builder()
                .url(url)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .referrer(referer)
                .clickedAt(LocalDateTime.now())
                .build();
                
        clickEventRepository.save(clickEvent);
        
        // Also update the denormalized count on the Url table
        urlRepository.findById(url.getId()).ifPresent(u -> {
            u.setClickCount(u.getClickCount() + 1);
            urlRepository.save(u);
        });
    }
}
