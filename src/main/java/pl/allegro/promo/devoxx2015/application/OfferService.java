package pl.allegro.promo.devoxx2015.application;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import pl.allegro.promo.devoxx2015.domain.Offer;
import pl.allegro.promo.devoxx2015.domain.OfferRepository;
import pl.allegro.promo.devoxx2015.domain.PhotoScoreSource;

import java.util.List;

@Component
public class OfferService {

    private final OfferRepository offerRepository;
    private final PhotoScoreSource photoScoreSource;

    @Autowired
    public OfferService(OfferRepository offerRepository, PhotoScoreSource photoScoreSource) {
        this.offerRepository = offerRepository;
        this.photoScoreSource = photoScoreSource;
    }

    public void processOffers(List<OfferPublishedEvent> events) {
        events.parallelStream().forEach(
                (event) -> {
                    try {
                        double score = photoScoreSource.getScore(event.getPhotoUrl());
                        if (score >= 0.7) {
                            offerRepository.save(
                                    new Offer(
                                            event.getId(), event.getTitle(), event.getPhotoUrl(), score));
                        }
                    } catch (Exception e) {
                        offerRepository.save(
                                new Offer(
                                        event.getId(), event.getTitle(), event.getPhotoUrl(), 0.7));
                    }
                });
    }

    public List<Offer> getOffers() {
        return offerRepository.findAll(new Sort(new Sort.Order(Sort.Direction.DESC, "photoScore")));
    }
}
