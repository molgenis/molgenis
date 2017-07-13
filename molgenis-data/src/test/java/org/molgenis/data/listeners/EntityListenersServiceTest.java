package org.molgenis.data.listeners;

import org.mockito.Mockito;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EntityListenersServiceTest
{
	private EntityListenersService entityListenersService = new EntityListenersService();

	@Test(expectedExceptions = NullPointerException.class)
	public void registerNullPointer()
	{
		entityListenersService.register(null);
	}

	@Test
	public void registerTest()
	{
		String repoFullName = "EntityRepo";
		entityListenersService.register(repoFullName);
		EntityListener entityListener1 = Mockito.when(Mockito.mock(EntityListener.class).getEntityId())
												.thenReturn(1)
												.getMock();
		EntityListener entityListener2 = Mockito.when(Mockito.mock(EntityListener.class).getEntityId())
												.thenReturn(2)
												.getMock();
		entityListenersService.addEntityListener(repoFullName, entityListener1);
		entityListenersService.addEntityListener(repoFullName, entityListener2);
		Assert.assertFalse(entityListenersService.isEmpty(repoFullName));
		entityListenersService.register(repoFullName);
		Assert.assertFalse(entityListenersService.isEmpty(repoFullName));
		entityListenersService.removeEntityListener(repoFullName, entityListener1);
		entityListenersService.removeEntityListener(repoFullName, entityListener2);
		Assert.assertTrue(entityListenersService.isEmpty(repoFullName));
	}

	@Test
	public void updateEntitiesTest()
	{
		String repoFullName = "EntityRepo";
		Entity entity1 = Mockito.mock(Entity.class);
		Entity entity2 = Mockito.mock(Entity.class);
		entityListenersService.register(repoFullName);
		EntityListener entityListener1 = Mockito.when(Mockito.mock(EntityListener.class).getEntityId())
												.thenReturn(1)
												.getMock();
		EntityListener entityListener2 = Mockito.when(Mockito.mock(EntityListener.class).getEntityId())
												.thenReturn(2)
												.getMock();
		Mockito.when(entity1.getIdValue()).thenReturn(1).getMock();
		Mockito.when(entity2.getIdValue()).thenReturn(2).getMock();
		entityListenersService.addEntityListener(repoFullName, entityListener1);
		entityListenersService.addEntityListener(repoFullName, entityListener2);
		entityListenersService.updateEntities(repoFullName, Stream.of(entity1, entity2))
							  .collect(Collectors.toList());
		Mockito.verify(entityListener1).postUpdate(entity1);
		Mockito.verify(entityListener2).postUpdate(entity2);
		entityListenersService.removeEntityListener(repoFullName, entityListener1);
		entityListenersService.removeEntityListener(repoFullName, entityListener2);
		Assert.assertTrue(entityListenersService.isEmpty(repoFullName));
	}

	@Test
	public void updateEntityTest()
	{
		String repoFullName = "EntityRepo";
		Entity entity = Mockito.mock(Entity.class);
		EntityListener entityListener1 = Mockito.when(Mockito.mock(EntityListener.class).getEntityId())
												.thenReturn(1)
												.getMock();
		EntityListener entityListener2 = Mockito.when(Mockito.mock(EntityListener.class).getEntityId())
												.thenReturn(1)
												.getMock();
		Mockito.when(entity.getIdValue()).thenReturn(1).getMock();
		entityListenersService.register(repoFullName);
		entityListenersService.addEntityListener(repoFullName, entityListener1);
		entityListenersService.addEntityListener(repoFullName, entityListener2);
		entityListenersService.updateEntity(repoFullName, entity);
		Mockito.verify(entityListener1).postUpdate(entity);
		Mockito.verify(entityListener2).postUpdate(entity);
		entityListenersService.removeEntityListener(repoFullName, entityListener1);
		entityListenersService.removeEntityListener(repoFullName, entityListener2);
		Assert.assertTrue(entityListenersService.isEmpty(repoFullName));
	}

	@Test
	public void addEntityListenerTest()
	{
		String repoFullName = "EntityRepo";
		EntityListener entityListener = Mockito.when(Mockito.mock(EntityListener.class).getEntityId())
											   .thenReturn(1)
											   .getMock();
		entityListenersService.register(repoFullName);
		entityListenersService.addEntityListener(repoFullName, entityListener);
		Mockito.verify(entityListener).getEntityId();
		entityListenersService.removeEntityListener(repoFullName, entityListener);
		Assert.assertTrue(entityListenersService.isEmpty(repoFullName));
	}

	@Test
	public void removeEntityListenerTest()
	{
		String repoFullName = "EntityRepo";
		EntityListener entityListener = Mockito.when(Mockito.mock(EntityListener.class).getEntityId())
											   .thenReturn(1)
											   .getMock();
		entityListenersService.register(repoFullName);
		entityListenersService.addEntityListener(repoFullName, entityListener);
		Assert.assertFalse(entityListenersService.isEmpty(repoFullName));
		entityListenersService.removeEntityListener(repoFullName, entityListener);
		Assert.assertTrue(entityListenersService.isEmpty(repoFullName));
	}

	@Test
	public void isEmptyTest()
	{
		String repoFullName = "EntityRepo";
		entityListenersService.register(repoFullName);
		Assert.assertTrue(entityListenersService.isEmpty(repoFullName));
	}

	@Test
	public void verifyRepoRegistered()
	{
		this.entityListenersService = new EntityListenersService();
		String repoFullName = "EntityRepo";
		EntityListener entityListener = Mockito.when(Mockito.mock(EntityListener.class).getEntityId())
											   .thenReturn(1)
											   .getMock();
		try
		{
			entityListenersService.addEntityListener(repoFullName, entityListener);
		}
		catch (MolgenisDataException mde)
		{
			entityListenersService.register(repoFullName);
			Assert.assertTrue(entityListenersService.isEmpty(repoFullName));
			Assert.assertEquals(mde.getMessage(),
					"Repository [EntityRepo] is not registered, please contact your administrator");
			return;
		}
		Assert.fail();
	}

	@Test
	public void noExceptionsStressTest()
	{
		List<Thread> ts = new ArrayList<>();
		ts.add(new NewThread("EntityRepo1", 0, 10).getThread());
		ts.add(new NewThread("EntityRepo1", 0, 10).getThread());
		ts.add(new NewThread("EntityRepo2", 0, 10).getThread());
		ts.add(new NewThread("EntityRepo2", 0, 10).getThread());

		ts.forEach(t ->
		{
			try
			{
				t.join();
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		});

		ts.forEach(t -> Assert.assertFalse(t.isAlive()));

		Assert.assertTrue(entityListenersService.isEmpty("EntityRepo1"));
		Assert.assertTrue(entityListenersService.isEmpty("EntityRepo2"));
	}

	private class NewThread implements Runnable
	{
		String repoFullName;
		int minIdRange;
		int maxIdRange;
		Thread t;

		NewThread(String repoFullName, int minIdRange, int maxIdRange)
		{
			this.repoFullName = repoFullName;
			if (maxIdRange <= minIdRange) throw new RuntimeException("max must be larger than min");
			this.minIdRange = minIdRange;
			this.maxIdRange = maxIdRange;
			t = new Thread(this);
			t.start();
		}

		public Thread getThread()
		{
			return t;
		}

		public void run()
		{
			for (int i = minIdRange; i < maxIdRange; i++)
			{
				entityListenersService.register(repoFullName);
				EntityListener entityListener = Mockito.when(Mockito.mock(EntityListener.class).getEntityId())
													   .thenReturn(i)
													   .getMock();
				entityListenersService.addEntityListener(repoFullName, entityListener);
				entityListenersService.removeEntityListener(repoFullName, entityListener);
			}
		}
	}
}
