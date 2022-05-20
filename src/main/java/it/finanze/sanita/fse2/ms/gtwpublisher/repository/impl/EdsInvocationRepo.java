package it.finanze.sanita.fse2.ms.gtwpublisher.repository.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import it.finanze.sanita.fse2.ms.gtwpublisher.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtwpublisher.repository.IEdsInvocationRepo;
import it.finanze.sanita.fse2.ms.gtwpublisher.repository.entity.IniEdsInvocationETY;
import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class EdsInvocationRepo implements IEdsInvocationRepo {

	/**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = 7040678303037387997L;

	@Autowired
	private MongoTemplate mongoTemplate;

	@Override
	public IniEdsInvocationETY findByTransactionId(final String transactionId) {
		IniEdsInvocationETY out = null;
		try {
			Query query = new Query();
			query.addCriteria(Criteria.where("transaction_id").is(transactionId));
			out = mongoTemplate.findOne(query, IniEdsInvocationETY.class);
		} catch(Exception ex) {
			log.error("Error while running find by transaction id query : " , ex);
			throw new BusinessException("Error while running find by transaction id query : " , ex);
		}
		return out;
	}
	
	 
}
