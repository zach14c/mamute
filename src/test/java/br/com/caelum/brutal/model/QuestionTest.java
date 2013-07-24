package br.com.caelum.brutal.model;

import static br.com.caelum.brutal.model.UpdateStatus.PENDING;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import br.com.caelum.brutal.builder.QuestionBuilder;
import br.com.caelum.brutal.dao.TestCase;

public class QuestionTest  extends TestCase{
	private QuestionBuilder question = new QuestionBuilder();

	@Test(expected = RuntimeException.class)
	public void can_not_be_marked_as_solved_by_the_an_answer_that_is_not_mine() {
		Question shouldILiveForever = question.build();
		Answer yes = answer("", null, null);
		shouldILiveForever.markAsSolvedBy(yes);
	}

	@Test
	public void can_be_marked_as_solved_by_the_an_answer_that_is_mine() {
		Question shouldILiveForever = question.build();
		Answer yes = answer("my answer", shouldILiveForever, null);
		
		shouldILiveForever.markAsSolvedBy(yes);
		
		assertEquals(yes, shouldILiveForever.getSolution());
	}
	
	@Test
	public void should_not_be_touched_when_marked_as_solved() {
		Question shouldILiveForever = question.build();
		User touchedBy = shouldILiveForever.getLastTouchedBy();
		User leo = user("", "");
		Answer yes = answer("my answer", shouldILiveForever, leo);
		
		assertEquals(User.GHOST, shouldILiveForever.getLastTouchedBy());

		shouldILiveForever.markAsSolvedBy(yes);
		
		assertEquals(touchedBy, shouldILiveForever.getLastTouchedBy());
	}

	@Test
	public void should_remove_vote_values_and_update_vote_count() {
		Question myQuestion = question.build();
		assertEquals(0l, myQuestion.getVoteCount());
		Vote firstVote = new Vote(null, VoteType.UP);
		myQuestion.substitute(null, firstVote);
		assertEquals(1l, myQuestion.getVoteCount());
		myQuestion.substitute(firstVote, new Vote(null, VoteType.DOWN));
		assertEquals(-1l, myQuestion.getVoteCount());
		myQuestion.substitute(null, new Vote(null, VoteType.DOWN));
		assertEquals(-2l, myQuestion.getVoteCount());
	}
	
	@Test
	public void should_be_touched_by_original_author_after_edit() throws Exception {
		User artur = user("artur", "artur@x.com");
		artur.setId(1l);
		Question comoFaz = question.withTitle("titulo").withDescription("descricao").withAuthor(artur).build();
		User leo = user("leo", "leo@x.com");
		leo.setId(2l);
		comoFaz.updateWith(new QuestionInformationBuilder().with(leo).build());
		assertEquals(comoFaz.getLastTouchedBy().getId(), artur.getId());
	}
	
	@Test
	public void should_update_information_status_and_last_touched_by() throws Exception {
		User artur = user("", "");
		User leo = user("", "");
		artur.setId(1l);
		leo.setId(2l);
		
		Question comoFaz = question.withTitle("titulo").withDescription("descricao").withAuthor(artur).build();
		QuestionInformation comoFazEditedInformation = new QuestionInformationBuilder().with(leo).build();
		comoFaz.updateWith(comoFazEditedInformation);
		comoFaz.approve(comoFazEditedInformation);
		
		assertEquals(comoFaz.getLastTouchedBy().getId(), leo.getId());
	}
	
	@Test
	public void should_set_author_only_once() throws Exception {
		Question q = question.build();
		User original = user("original", "original@brutal.com");
		q.setAuthor(original);
		User other = user("other", "other@brutal.com");
		q.setAuthor(other);
		
		assertEquals(original, q.getAuthor());
	}
	
	@Test
	public void should_verify_that_a_user_already_flagged_question() throws Exception {
		Question q = question.build();
		User author = user("author", "author@brutal.com", 1l);
		User other = user("other", "other@brutal.com", 2l);
		Flag flag = flag(FlagType.OTHER, author);
		q.add(flag);
		
		assertTrue(q.alreadyFlaggedBy(author));
		assertFalse(q.alreadyFlaggedBy(other));
	}
	
	@Test
	public void should_verify_that_a_user_already_answered_question() throws Exception {
		Question q = question.build();
		User author = user("author", "author@brutal.com", 1l);
		User other = user("other", "other@brutal.com", 2l);
		answer("my answer", q, author);
		
		assertTrue(q.alreadyAnsweredBy(author));
		assertFalse(q.alreadyAnsweredBy(other));
	}
	
	@Test
	public void should_update_information_and_tag_usage_count() throws Exception {
		Tag ruby = tag("ruby");
		Tag java = tag("java");
		Question q = question.withTag(ruby).build();
		QuestionInformation approved = new QuestionInformationBuilder().withTag(java).build();
		q.updateApproved(approved);
		
		assertEquals(0l, ruby.getUsageCount().longValue());
		assertEquals(1l, java.getUsageCount().longValue());
	}
	
	@Test
	public void should_return_true_if_question_has_pending_edits() throws Exception {
		Tag ruby = tag("ruby");
		Tag java = tag("java");
		Question q = question.withTag(ruby).build();
		assertFalse(q.hasPendingEdits());

		QuestionInformation approved = new QuestionInformationBuilder().withTag(java).build();
		q.enqueueChange(approved, PENDING);
		
		assertTrue(q.hasPendingEdits());
	}

	@Test
	public void should_verify_if_is_visible_for_author() {
		User author = user("leo", "leo@leo");
		Question shouldILiveForever = question.withAuthor(author).build();
		shouldILiveForever.remove();
		boolean isVisibleForAuthor = shouldILiveForever.isVisibleFor(author);
		assertTrue(isVisibleForAuthor);
	}
	
	@Test
	public void should_verify_if_is_visible_for_moderator() {
		Question shouldILiveForever = question.build();
		shouldILiveForever.remove();
		boolean isVisibleForModerator = shouldILiveForever.isVisibleFor(user("leo", "leo@leo").asModerator());
		assertTrue(isVisibleForModerator);
	}
	
	@Test
	public void should_verify_if_is_visible_for_null() {
		Question shouldILiveForever = question.build();
		shouldILiveForever.remove();
		boolean isVisibleForNull = shouldILiveForever.isVisibleFor(null);
		assertFalse(isVisibleForNull);
	}
	
	@Test
	public void should_verify_if_is_visible_when_visible() {
		Question shouldILiveForever = question.build();
		boolean isVisibleForModerator = shouldILiveForever.isVisibleFor(null);
		assertTrue(isVisibleForModerator);
	}
	
	@Test
	public void shouldNotTrimShortMeta() throws Exception {
		String title = "0123456789";
		String description = "description";
		Question q = question
				.withTitle(title)
				.withDescription(description)
				.build();
		assertTrue(q.getMetaDescription().contains(title));
		assertTrue(q.getMetaDescription().contains(description));
	}
	
	@Test
	public void should_trim_big_meta() throws Exception {
		String title = bigString('a', 100);
		String description = bigString('b', 10000);
		Question q = question
				.withTitle(title)
				.withDescription(description)
				.build();
		assertTrue(q.getMetaDescription().contains(title));
		assertFalse(q.getMetaDescription().contains(description));
	}

	private String bigString(char c, int repetitions) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < repetitions; i++) {
			builder.append(c);
		}
		return builder.toString();
	}
	
}
