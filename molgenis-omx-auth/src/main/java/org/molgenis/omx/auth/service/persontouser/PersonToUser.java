/* Date:        February 2, 2010
 * Template:	PluginScreenJavaTemplateGen.java.ftl
 * generator:   org.molgenis.generators.ui.PluginScreenJavaTemplateGen 3.3.2-testing
 * 
 * THIS FILE IS A TEMPLATE. PLEASE EDIT :-)
 */

package org.molgenis.omx.auth.service.persontouser;

import java.util.List;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
import org.molgenis.framework.security.SimpleLogin;
import org.molgenis.framework.server.MolgenisRequest;
import org.molgenis.framework.ui.PluginModel;
import org.molgenis.framework.ui.ScreenController;
import org.molgenis.framework.ui.ScreenMessage;
import org.molgenis.model.elements.Field;
import org.molgenis.omx.auth.MolgenisGroup;
import org.molgenis.omx.auth.MolgenisRoleGroupLink;
import org.molgenis.omx.auth.MolgenisUser;
import org.molgenis.omx.auth.Person;
import org.molgenis.util.Entity;

public class PersonToUser extends PluginModel
{
	private static final long serialVersionUID = 1L;

	private final PersonToUserModel model = new PersonToUserModel();

	public PersonToUserModel getMyModel()
	{
		return model;
	}

	public PersonToUser(String name, ScreenController<?> parent)
	{
		super(name, parent);
	}

	@Override
	public String getViewName()
	{
		return "PersonToUser";
	}

	@Override
	public String getViewTemplate()
	{
		return "templates/" + PersonToUser.class.getName().replace('.', '/') + ".ftl";
	}

	@Override
	public void handleRequest(Database db, MolgenisRequest request)
	{
		if (request.getString("__action") != null)
		{

			System.out.println("*** handleRequest __action: " + request.getString("__action"));
			String action = request.getString("__action");

			try
			{
				if (action.equals("upgrade"))
				{
					// get person id
					int pid = request.getInt("personId");

					// get group id if possible
					int gid = -1;
					if (request.getInt("groupId") != null)
					{
						gid = request.getInt("groupId");
					}

					// get the person from db
					Person p = db.find(Person.class, new QueryRule(Person.ID, Operator.EQUALS, pid)).get(0);

					// copy properties except ID and __Type, plus 'active' and
					// 'password'
					MolgenisUser mu = new MolgenisUser();
					for (String f : p.getFields(true))
					{
						if (!f.equals(Field.TYPE_FIELD))
						{
							mu.set(f, p.get(f));
						}

					}
					mu.setActive(true);
					mu.setPassword("changeme");

					try
					{
						// remove person
						db.remove(p);
						// add as user
						db.add(mu);
					}// FIXME: Hack to allow referenced persons to be upgraded
					catch (org.molgenis.framework.db.DatabaseException de)
					{
						// if this person is referred to, this happens:
						// Integrity constraint violation SYS_FK_1350 table:
						// PROTOCOLAPPLICATION_PERFORMER in statement [DELETE
						// FROM person WHERE id=?]

						// first add as user, need other name not to conflict
						mu.setName(mu.getName() + "tmp");
						db.add(mu);

						// re-ref the ProtocolApplication_Performer
						// FIXME: dangerous to use hardcoded strings here, must
						// move this code elsewhere in its original form
						Class<? extends Entity> ProtocolApplication_Performer = db
								.getClassForName("ProtocolApplication_Performer");
						List<? extends Entity> paLinks = db.find(ProtocolApplication_Performer, new QueryRule(
								"Performer", Operator.EQUALS, p.getId()));
						for (Entity paLink : paLinks)
						{
							paLink.set("Performer_id", mu.getId());
						}
						db.update(paLinks);

						// now remove person
						db.remove(p);

						// now set proper name for user
						mu.setName(mu.getName().substring(0, mu.getName().length() - 3));
						db.update(mu);
					}

					// put user in the group if applicable
					if (gid != -1)
					{
						MolgenisGroup mg = db.find(MolgenisGroup.class,
								new QueryRule(MolgenisGroup.ID, Operator.EQUALS, gid)).get(0);
						System.out.println("** mg = " + mg.getName());
						MolgenisRoleGroupLink mr = new MolgenisRoleGroupLink();
						mr.setRole(mu);
						mr.setGroup(mg);
						db.add(mr);
					}

					// great success
					this.setMessages(new ScreenMessage("The selected person is now a user with password 'changeme'.",
							true));

				}

			}
			catch (Exception e)
			{
				e.printStackTrace();
				this.setMessages(new ScreenMessage(e.getMessage() != null ? e.getMessage() : "null", false));
			}
		}
	}

	@Override
	public void reload(Database db)
	{

		List<Person> personList;
		List<MolgenisGroup> groupList;

		try
		{
			personList = db.find(Person.class, new QueryRule(Field.TYPE_FIELD, Operator.EQUALS, "Person"));
			this.model.setPersonList(personList);
			groupList = db.find(MolgenisGroup.class);
			this.model.setGroupList(groupList);

		}
		catch (DatabaseException e)
		{
			e.printStackTrace();
			this.setMessages(new ScreenMessage(e.getMessage() != null ? e.getMessage() : "null", false));
		}

	}

	@Override
	public boolean isVisible()
	{
		if (this.getController().getApplicationController().getLogin() instanceof SimpleLogin)
		{
			return false;
		}
		return super.isVisible();
	}

}
