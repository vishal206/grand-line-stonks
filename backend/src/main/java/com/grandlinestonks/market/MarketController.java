package com.grandlinestonks.market;

import com.grandlinestonks.auth.AuthenticatedUser;
import com.grandlinestonks.market.dto.BetRequest;
import com.grandlinestonks.market.dto.BetResponse;
import com.grandlinestonks.market.dto.CreateMarketRequest;
import com.grandlinestonks.market.dto.MarketResponse;
import com.grandlinestonks.market.dto.PricePointResponse;
import com.grandlinestonks.market.dto.ResolveRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/markets")
public class MarketController {

    private final MarketService marketService;
    private final BetService betService;
    private final SettlementService settlementService;

    public MarketController(
            MarketService marketService,
            BetService betService,
            SettlementService settlementService) {
        this.marketService = marketService;
        this.betService = betService;
        this.settlementService = settlementService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MarketResponse create(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody CreateMarketRequest request) {
        return marketService.create(user.userId(), request);
    }

    @PostMapping("/{id}/open")
    public MarketResponse open(
            @AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id) {
        return marketService.open(user.userId(), id);
    }

    @PostMapping("/{id}/close")
    public MarketResponse close(
            @AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id) {
        return marketService.close(user.userId(), id);
    }

    @PostMapping("/{id}/resolve")
    public MarketResponse resolve(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long id,
            @Valid @RequestBody ResolveRequest request) {
        return settlementService.resolve(user.userId(), id, request.winningOutcomeId());
    }

    @PostMapping("/{id}/settle")
    public MarketResponse settle(
            @AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id) {
        return settlementService.settle(user.userId(), id);
    }

    @PostMapping("/{id}/cancel")
    public MarketResponse cancel(
            @AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id) {
        return settlementService.cancel(user.userId(), id);
    }

    @GetMapping
    public Page<MarketResponse> list(
            @RequestParam(required = false) MarketStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return marketService.list(status, pageable);
    }

    @GetMapping("/{id}")
    public MarketResponse get(@PathVariable Long id) {
        return marketService.get(id);
    }

    @GetMapping("/{id}/history")
    public List<PricePointResponse> history(@PathVariable Long id) {
        return marketService.priceHistory(id);
    }

    @PostMapping("/{id}/bets")
    @ResponseStatus(HttpStatus.CREATED)
    public BetResponse bet(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long id,
            @Valid @RequestBody BetRequest request) {
        return betService.placeBet(user.userId(), id, request);
    }
}
