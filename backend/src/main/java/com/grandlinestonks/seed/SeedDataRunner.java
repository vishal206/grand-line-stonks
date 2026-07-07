package com.grandlinestonks.seed;

import com.grandlinestonks.auth.AuthService;
import com.grandlinestonks.market.BetService;
import com.grandlinestonks.market.MarketRepository;
import com.grandlinestonks.market.MarketService;
import com.grandlinestonks.market.SettlementService;
import com.grandlinestonks.market.dto.BetRequest;
import com.grandlinestonks.market.dto.CreateMarketRequest;
import com.grandlinestonks.market.dto.MarketResponse;
import com.grandlinestonks.user.UserRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty("app.seed.enabled")
public class SeedDataRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(SeedDataRunner.class);
    private static final String SEED_PASSWORD = "grandline123";

    private final AuthService authService;
    private final UserRepository userRepository;
    private final MarketRepository marketRepository;
    private final MarketService marketService;
    private final BetService betService;
    private final SettlementService settlementService;

    public SeedDataRunner(
            AuthService authService,
            UserRepository userRepository,
            MarketRepository marketRepository,
            MarketService marketService,
            BetService betService,
            SettlementService settlementService) {
        this.authService = authService;
        this.userRepository = userRepository;
        this.marketRepository = marketRepository;
        this.marketService = marketService;
        this.betService = betService;
        this.settlementService = settlementService;
    }

    @Override
    public void run(String... args) {
        if (marketRepository.count() > 0) {
            log.info("seed skipped, markets already exist");
            return;
        }
        Long owner = seedUser("nami_the_broker");
        List<Long> bettors = List.of(
                seedUser("zoro_bets"), seedUser("usopp_brave"),
                seedUser("sanji_cook"), seedUser("chopper_doc"));

        Random random = new Random(1);
        openWithBets(owner, bettors, random,
                "Will the Straw Hats reach Laugh Tale before the year ends?", List.of("Yes", "No"));
        openWithBets(owner, bettors, random,
                "Who takes the top bounty after the next big clash?",
                List.of("Luffy", "A Marine Admiral", "Someone unexpected"));
        openWithBets(owner, bettors, random,
                "Will a new crew member join before the next island?", List.of("Yes", "No"));
        openWithBets(owner, bettors, random,
                "Does the Grand Line weather stay calm for the crossing?",
                List.of("Calm seas", "Storms ahead"));
        marketService.create(owner, new CreateMarketRequest(
                "Will Buggy stumble into another promotion?",
                List.of("Yes", "No"), new BigDecimal("100")));
        settledMarket(owner, bettors, random);
        log.info("seed complete: demo users password is {}", SEED_PASSWORD);
    }

    private Long seedUser(String username) {
        authService.signup(username, SEED_PASSWORD);
        return userRepository.findByUsername(username).orElseThrow().getId();
    }

    private MarketResponse openWithBets(
            Long owner, List<Long> bettors, Random random, String question, List<String> outcomes) {
        MarketResponse market = marketService.create(
                owner, new CreateMarketRequest(question, outcomes, new BigDecimal("100")));
        marketService.open(owner, market.id());
        MarketResponse opened = marketService.get(market.id());
        int betCount = 6 + random.nextInt(6);
        for (int i = 0; i < betCount; i++) {
            Long bettor = bettors.get(random.nextInt(bettors.size()));
            Long outcomeId = opened.outcomes().get(random.nextInt(opened.outcomes().size())).id();
            BigDecimal amount = BigDecimal.valueOf(5 + random.nextInt(46));
            betService.placeBet(bettor, market.id(), new BetRequest(
                    outcomeId, amount, "seed:" + market.id() + ":" + i));
        }
        return opened;
    }

    private void settledMarket(Long owner, List<Long> bettors, Random random) {
        MarketResponse market = openWithBets(owner, bettors, random,
                "Did the crew survive the last raid unscathed?", List.of("Yes", "No"));
        marketService.close(owner, market.id());
        settlementService.resolve(owner, market.id(), market.outcomes().get(0).id());
        settlementService.settle(owner, market.id());
    }
}
