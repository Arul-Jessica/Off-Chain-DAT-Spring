package org.tokenization.repository;

import org.tokenization.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {
    // We can add custom queries here later, e.g.:
    List<Wallet> findByPartyId(Long partyId);
}