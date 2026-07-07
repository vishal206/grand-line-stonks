package com.grandlinestonks.transfer;

import com.grandlinestonks.auth.AuthenticatedUser;
import com.grandlinestonks.transfer.dto.TransferRequest;
import com.grandlinestonks.transfer.dto.TransferResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transfers")
public class TransferController {

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransferResponse transfer(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody TransferRequest request) {
        return transferService.transfer(
                user.userId(), request.toUsername(), request.amount(), request.idempotencyKey());
    }
}
