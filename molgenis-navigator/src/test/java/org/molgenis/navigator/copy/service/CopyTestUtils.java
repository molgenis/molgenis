package org.molgenis.navigator.copy.service;

import static org.mockito.Mockito.when;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.molgenis.data.populate.IdGenerator;

class CopyTestUtils {
  static void setupPredictableIdGeneratorMock(IdGenerator idGeneratorMock) {
    when(idGeneratorMock.generateId())
        .thenAnswer(
            new Answer() {
              private int count = 0;

              @Override
              public Object answer(InvocationOnMock invocation) {
                count++;
                return "id" + count;
              }
            });
  }
}
