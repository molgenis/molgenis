package org.molgenis.omx.controller;

import java.util.List;

import javax.annotation.Nullable;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.util.ShoppingCart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.WebApplicationContext;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

@Controller
@Scope(WebApplicationContext.SCOPE_REQUEST)
@RequestMapping("/cart")
public class ShoppingCartController
{
	@Autowired
	private ShoppingCart shoppingCart;

	@Autowired
	private Database database;

	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody
	public FeaturesResponse getCart()
	{
		List<Integer> featureIds = shoppingCart.getCart();
		return new FeaturesResponse(Lists.transform(featureIds, new Function<Integer, FeatureResponse>()
		{
			@Override
			@Nullable
			public FeatureResponse apply(@Nullable Integer featureId)
			{
				ObservableFeature feature;
				try
				{
					feature = database.findById(ObservableFeature.class, featureId);
				}
				catch (DatabaseException e)
				{
					throw new RuntimeException(e);
				}
				return new FeatureResponse(feature);
			}
		}));
	}

	@RequestMapping(method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	public void addToCart(@Valid @RequestBody FeaturesRequest featuresRequest)
	{
		shoppingCart.addToCart(Lists.transform(featuresRequest.getFeatures(), new Function<FeatureRequest, Integer>()
		{
			@Override
			@Nullable
			public Integer apply(@Nullable FeatureRequest featureRequest)
			{
				return featureRequest.getFeature();
			}
		}));
	}

	@RequestMapping(value = "/replace", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	public void emptyAndAddToCart(@Valid @RequestBody FeaturesRequest featuresRequest)
	{
		shoppingCart.emptyAndAddToCart(Lists.transform(featuresRequest.getFeatures(),
				new Function<FeatureRequest, Integer>()
				{
					@Override
					@Nullable
					public Integer apply(@Nullable FeatureRequest featureRequest)
					{
						return featureRequest.getFeature();
					}
				}));
	}

	private static class FeaturesRequest
	{
		@NotNull
		private List<FeatureRequest> features;

		public List<FeatureRequest> getFeatures()
		{
			return features;
		}

		@SuppressWarnings("unused")
		public void setFeatures(List<FeatureRequest> features)
		{
			this.features = features;
		}
	}

	private static class FeatureRequest
	{
		@NotNull
		private Integer feature;

		public Integer getFeature()
		{
			return feature;
		}

		@SuppressWarnings("unused")
		public void setFeature(Integer feature)
		{
			this.feature = feature;
		}
	}

	private static class FeaturesResponse
	{
		private List<FeatureResponse> features;

		public FeaturesResponse(List<FeatureResponse> features)
		{
			this.features = features;
		}

		@SuppressWarnings("unused")
		public List<FeatureResponse> getFeatureFilters()
		{
			return features;
		}

		@SuppressWarnings("unused")
		public void setFeatureFilters(List<FeatureResponse> featureFilters)
		{
			this.features = featureFilters;
		}
	}

	private static class FeatureResponse
	{
		private Integer id;
		private String name;
		private String description;

		public FeatureResponse(ObservableFeature feature)
		{
			this.id = feature.getId();
			this.name = feature.getName();
			this.description = feature.getDescription();
		}

		@SuppressWarnings("unused")
		public Integer getId()
		{
			return id;
		}

		@SuppressWarnings("unused")
		public void setId(Integer id)
		{
			this.id = id;
		}

		@SuppressWarnings("unused")
		public String getName()
		{
			return name;
		}

		@SuppressWarnings("unused")
		public void setName(String name)
		{
			this.name = name;
		}

		@SuppressWarnings("unused")
		public String getDescription()
		{
			return description;
		}

		@SuppressWarnings("unused")
		public void setDescription(String description)
		{
			this.description = description;
		}
	}
}
