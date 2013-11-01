package org.molgenis.omx.cart;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.molgenis.data.DataService;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.util.ShoppingCart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

@Controller
@RequestMapping("/cart")
public class ShoppingCartController
{
	@Autowired
	private ShoppingCart shoppingCart;

	@Autowired
	private DataService dataService;

	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody
	public FeaturesResponse getCart()
	{
		List<Integer> featureIds = shoppingCart.getCart();
		return new FeaturesResponse(Lists.transform(featureIds, new Function<Integer, FeatureResponse>()
		{
			@Override
			@Nullable
			public FeatureResponse apply(@Nullable
			Integer featureId)
			{
				ObservableFeature feature = dataService.findOne(ObservableFeature.ENTITY_NAME, featureId);
				return new FeatureResponse(feature);
			}
		}));
	}

	@RequestMapping(value = "/add", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	public void addToCart(@Valid
	@RequestBody
	FeaturesRequest featuresRequest)
	{
		shoppingCart.addToCart(Lists.transform(featuresRequest.getFeatures(), new Function<FeatureRequest, Integer>()
		{
			@Override
			@Nullable
			public Integer apply(@Nullable
			FeatureRequest featureRequest)
			{
				return featureRequest != null ? featureRequest.getFeature() : null;
			}
		}));
	}

	@RequestMapping(value = "/empty", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	public void emptyCart()
	{
		shoppingCart.emptyCart();
	}

	@RequestMapping(value = "/remove", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	public void removeFromCart(@Valid
	@RequestBody
	FeaturesRequest featuresRequest)
	{
		shoppingCart.removeFromCart(Lists.transform(featuresRequest.getFeatures(),
				new Function<FeatureRequest, Integer>()
				{
					@Override
					@Nullable
					public Integer apply(@Nullable
					FeatureRequest featureRequest)
					{
						return featureRequest != null ? featureRequest.getFeature() : null;
					}
				}));
	}

	@RequestMapping(value = "/replace", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	public void emptyAndAddToCart(@Valid
	@RequestBody
	FeaturesRequest featuresRequest)
	{
		shoppingCart.emptyAndAddToCart(Lists.transform(featuresRequest.getFeatures(),
				new Function<FeatureRequest, Integer>()
				{
					@Override
					@Nullable
					public Integer apply(@Nullable
					FeatureRequest featureRequest)
					{
						return featureRequest != null ? featureRequest.getFeature() : null;
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
		private Map<String, String> i18nDescription;

		public FeatureResponse(ObservableFeature feature)
		{
			this.id = feature.getId();
			this.name = feature.getName();
			String description = feature.getDescription();
			if (description != null && (!description.startsWith("{") || !description.endsWith("}")))
			{
				description = " {\"en\":\"" + description + "\"}";
			}
			this.i18nDescription = new Gson().fromJson(description, new TypeToken<Map<String, String>>()
			{
			}.getType());
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
		public Map<String, String> getI18nDescription()
		{
			return i18nDescription;
		}

		@SuppressWarnings("unused")
		public void setI18nDescription(Map<String, String> i18nDescription)
		{
			this.i18nDescription = i18nDescription;
		}
	}
}
