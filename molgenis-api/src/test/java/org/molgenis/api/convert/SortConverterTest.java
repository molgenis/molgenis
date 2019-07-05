// package org.molgenis.api.convert;
//
// import static java.util.Arrays.asList;
// import static org.testng.Assert.assertEquals;
//
// import org.molgenis.SortConverter;
// import org.molgenis.api.model.Sort;
// import org.molgenis.api.model.Sort.Order;
// import org.molgenis.api.model.Sort.Order.Direction;
// import org.testng.Assert;
// import org.testng.annotations.Test;
//
// public class SortConverterTest {
//  @Test
//  public void convertSingleAttrDefault() {
//    Assert.assertEquals(new SortConverter().convert("item"), Sort.create("item"));
//  }
//
//  @Test
//  public void convertSingleAttrAsc() {
//    assertEquals(new SortConverter().convert("+item"), Sort.create("item", Direction.ASC));
//  }
//
//  @Test
//  public void convertSingleAttrDesc() {
//    assertEquals(new SortConverter().convert("-item"), Sort.create("item", Direction.DESC));
//  }
//
//  @Test
//  public void convertMultiAttrDefault() {
//    assertEquals(
//        new SortConverter().convert("item0,item1"),
//        Sort.create(asList(Order.create("item0"), Order.create("item1"))));
//  }
//
//  @Test
//  public void convertMultiAttrAsc() {
//    assertEquals(
//        new SortConverter().convert("+item0,+item1"),
//        Sort.create(
//            asList(Order.create("item0", Direction.ASC), Order.create("item1", Direction.ASC))));
//  }
//
//  @Test
//  public void convertMultiAttrDesc() {
//    assertEquals(
//        new SortConverter().convert("-item0,-item1"),
//        Sort.create(
//            asList(Order.create("item0", Direction.DESC), Order.create("item1",
// Direction.DESC))));
//  }
//
//  @Test
//  public void convertMultiAttrAscAndDesc() {
//    assertEquals(
//        new SortConverter().convert("+item0,-item1"),
//        Sort.create(
//            asList(Order.create("item0", Direction.ASC), Order.create("item1", Direction.DESC))));
//  }
// }
