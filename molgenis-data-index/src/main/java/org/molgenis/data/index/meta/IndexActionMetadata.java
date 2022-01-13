package org.molgenis.data.index.meta;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.index.meta.IndexPackage.PACKAGE_INDEX;
import static org.molgenis.data.meta.AttributeType.ENUM;
import static org.molgenis.data.meta.AttributeType.TEXT;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_LABEL;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;

import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.SystemEntityType;
import org.springframework.stereotype.Component;

/**
 * The index action is used to describe the action that needs to be done to make a {@link
 * org.molgenis.data.Repository}'s index consistent again.
 */
@Component
public class IndexActionMetadata extends SystemEntityType {
  private static final String SIMPLE_NAME = "IndexAction";
  public static final String INDEX_ACTION = PACKAGE_INDEX + PACKAGE_SEPARATOR + SIMPLE_NAME;

  /** Is auto generated */
  public static final String ID = "id";

  /** The creation time of the index action. */
  public static final String CREATION_DATE_TIME = "creationDateTime";

  /** The time at which this index action was finished (for whichever reason) */
  public static final String END_DATE_TIME = "endDateTime";

  /** The transaction that caused this index action. */
  public static final String TRANSACTION_ID = "transactionId";

  /** The name of the entity type ID that needs to be indexed */
  public static final String ENTITY_TYPE_ID = "entityTypeId";

  /** Entity is filled when only one row of the entity "entityFullName" is indexed */
  public static final String ENTITY_ID = "entityId";

  /**
   * Enum: the status of index action.
   *
   * @see IndexStatus
   */
  public static final String INDEX_STATUS = "indexStatus";

  private final IndexPackage indexPackage;

  public IndexActionMetadata(IndexPackage indexPackage) {
    super(SIMPLE_NAME, PACKAGE_INDEX);
    this.indexPackage = requireNonNull(indexPackage);
  }

  @Override
  public void init() {
    setLabel("Index action");
    setPackage(indexPackage);

    addAttribute(ID, ROLE_ID).setAuto(true).setVisible(false);
    addAttribute(CREATION_DATE_TIME, ROLE_LABEL).setDataType(AttributeType.DATE_TIME).setAuto(true);
    addAttribute(END_DATE_TIME).setDataType(AttributeType.DATE_TIME).setNillable(true);
    addAttribute(TRANSACTION_ID)
        .setDescription("The id of the transaction that caused this index action");
    addAttribute(ENTITY_TYPE_ID)
        .setDescription("The id of the entity type that needs to be indexed (e.g. myEntityType).")
        .setNillable(false);
    addAttribute(ENTITY_ID)
        .setDescription("The id of the entity that needs to be indexed")
        .setDataType(TEXT)
        .setNillable(true);
    addAttribute(INDEX_STATUS)
        .setDescription("The status of index action")
        .setDataType(ENUM)
        .setEnumOptions(IndexStatus.class)
        .setNillable(false);
  }

  /** Index action status */
  public enum IndexStatus {
    /** index action is finished */
    FINISHED,
    /** index action is canceled */
    CANCELED,
    /** index action is skipped (because it is not necessary) */
    SKIPPED,
    /** index action failed */
    FAILED,
    /** index action is started */
    STARTED,
    /** index action is just created and is not yet processed */
    PENDING
  }
}
