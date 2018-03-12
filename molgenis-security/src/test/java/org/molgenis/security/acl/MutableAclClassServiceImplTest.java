package org.molgenis.security.acl;

import org.mockito.Mock;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.acls.model.AclCache;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertFalse;

public class MutableAclClassServiceImplTest extends AbstractMockitoTest
{
	@Mock
	private JdbcTemplate jdbcTemplate;
	@Mock
	private AclCache aclCache;
	private MutableAclClassServiceImpl mutableAclClassService;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		mutableAclClassService = new MutableAclClassServiceImpl(jdbcTemplate, aclCache);
	}

	@Test
	public void testCreateAclClass()
	{
		String type = "MyType";
		mutableAclClassService.createAclClass(type, String.class);
		verify(jdbcTemplate).update("insert into acl_class (class, class_id_type) values (?, ?)", type,
				"java.lang.String");
		verifyZeroInteractions(aclCache);
	}

	@Test
	public void testDeleteAclClass()
	{
		String type = "MyType";
		mutableAclClassService.deleteAclClass(type);
		verify(jdbcTemplate).update("delete from acl_class where class=?", type);
		verify(aclCache).clearCache();
	}

	@Test
	public void testHasAclClassTrue()
	{
		String type = "MyType";
		when(jdbcTemplate.queryForObject("select count(*) from acl_class WHERE class = ?", new Object[] { type },
				Integer.class)).thenReturn(1);
		assertTrue(mutableAclClassService.hasAclClass(type));
		verifyZeroInteractions(aclCache);
	}

	@Test
	public void testHasAclClassFalse()
	{
		String type = "MyType";
		when(jdbcTemplate.queryForObject("select count(*) from acl_class WHERE class = ?", new Object[] { type },
				Integer.class)).thenReturn(0);
		assertFalse(mutableAclClassService.hasAclClass(type));
		verifyZeroInteractions(aclCache);
	}

	@Test
	public void testGetAclClassTypes()
	{
		List<String> aclClassTypes = asList("MyType0", "MyType1");
		when(jdbcTemplate.queryForList("select class from acl_class", String.class)).thenReturn(aclClassTypes);
		assertEquals(mutableAclClassService.getAclClassTypes(), aclClassTypes);
		verifyZeroInteractions(aclCache);
	}
}