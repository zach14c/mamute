package br.com.caelum.brutal.model;

import static br.com.caelum.brutal.model.UpdateStatus.PENDING;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import br.com.caelum.brutal.builder.QuestionBuilder;

public class QuestionInformationTest {

	private QuestionInformationBuilder builder;
	private Question ruby;

	@Before
	public void setup(){
		builder = new QuestionInformationBuilder();
		ruby = new QuestionBuilder().withTitle("how do i program using ruby?").build();
	}
	
	@Test
	public void should_verify_if_is_before_current_information() {
		
		QuestionInformation version = builder.build();
		ruby.enqueueChange(version, PENDING);
		QuestionInformation infoByModerator = builder.build();
		ruby.approve(infoByModerator);
		
		assertTrue(version.isBeforeCurrent());
	}
	
	@Test
	public void should_verify_if_is_not_before_current_information() {
		
		QuestionInformation infoByModerator = builder.build();
		ruby.approve(infoByModerator);
		QuestionInformation version = builder.build();
		ruby.enqueueChange(version, PENDING);
		
		assertFalse(version.isBeforeCurrent());
	}

}