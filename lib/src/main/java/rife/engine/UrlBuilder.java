/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import rife.tools.StringUtils;

import java.io.File;
import java.util.*;

/**
 * {@code UrlBuilder} helps creating valid URLs that automatically
 * adapt based on the current execution {@code Context} and target {@code Route}.
 * <p>Using this class will ensure that the right state is always passed
 * on, and that the correct URL path is always used.
 * <p>{@code UrlBuilder} is not intended to be instantiated directly, but
 * rather through {@link Context#urlFor(Route)}.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public class UrlBuilder {
    private final Context context_;
    private final String rootUrl_;
    private final Route route_;
    private String pathInfo_;
    private final Map<String, String[]> parameters_ = new LinkedHashMap<>();
    private String fragment_;

    UrlBuilder(Context context, Route route) {
        context_ = context;
        rootUrl_ = context.webappRootUrl();
        route_ = route;
    }

    /**
     * Sets a particular pathinfo for this URL, it will automatically be encoded.
     * <p>If no pathinfo is manually set, the destination route has pathinfo
     * mapping configured, and the parameters are available to construct that
     * pathinfo, then RIFE2 will automatically create a correct pathinfo.
     *
     * @param pathInfo the explicit pathinfo to use
     * @return this {@code UrlBuilder} instance
     * @since 1.0
     */
    public UrlBuilder pathInfo(String pathInfo) {
        pathInfo_ = StringUtils.stripFromFront(pathInfo, "/");
        return this;
    }

    /**
     * Sets an outgoing parameter to a particular value.
     * <p>This will override any automatic outgoing parameters that are being published by the {@code Context}.
     *
     * @param name the name of the parameter
     * @param value the value of the parameter
     * @return this {@code UrlBuilder} instance
     * @since 1.0
     */
    public UrlBuilder param(String name, String value) {
        parameters_.put(name, new String[]{value});
        return this;
    }

    /**
     * Sets an outgoing parameter to a particular value array.
     * <p>This will override any automatic outgoing parameters that are being published by the {@code Context}.
     *
     * @param name the name of the parameter
     * @param value the value of the parameter
     * @return this {@code UrlBuilder} instance
     * @since 1.0
     */
    public UrlBuilder param(String name, String[] value) {
        parameters_.put(name, value);
        return this;
    }


    /**
     * Sets a parameters for all the entries in the provided parameter map.
     * <p>This will override any automatic outgoing parameters that are being published by the {@code Context}.
     *
     * @param parameters the map of parameters that will be set
     * @return this {@code UrlBuilder} instance
     * @since 1.0
     */
    public UrlBuilder params(Map<String, String[]> parameters) {
        parameters_.putAll(parameters);
        return this;
    }

    /**
     * Sets a particular fragment for this URL, it will be appended at the end, separated by a {@code #}.
     *
     * @param fragment the fragment to use
     * @return this {@code UrlBuilder} instance
     * @since 1.0
     */
    public UrlBuilder fragment(String fragment) {
        fragment_ = StringUtils.stripFromFront(fragment, "#");
        return this;
    }

    /**
     * Generates the string version of the current state of the {@code UrlBuilder}.
     *
     * @return the string of the URL that's being built
     * @since 1.0
     */
    public String toString() {
        final var url = new StringBuilder(rootUrl_);

        url.append(StringUtils.stripFromFront(route_.path(), "/"));

        var parameters = new LinkedHashMap<String, String[]>();

        // detect which parameters are annotation for output and input and retrieve those that correspond
        if (context_.route() instanceof RouteClass) {
            var out_params = RouteClass.getAnnotatedOutParameters(context_);
            if (context_.hasContinuationId()) {
                // only add the continuation ID if the route is going to the same element class
                if (context_.processedElement() != null &&
                    context_.processedElement().getClass() == route_.getElementClass()) {
                    out_params.put(SpecialParameters.CONT_ID, new String[]{context_.continuationId()});
                }
            }

            Set<String> in_params = new HashSet<>();
            in_params.add(SpecialParameters.CONT_ID);

            // input parameters
            if (route_ instanceof RouteClass route) {
                in_params.addAll(route.getAnnotatedInParameters());
            }

            // path info parameters
            if (route_.pathInfoHandling().type() == PathInfoType.MAP) {
                for (var mapping : route_.pathInfoHandling().mappings()) {
                    in_params.addAll(mapping.parameters());
                }
            }
            // retain the appropriate output parameters
            out_params.keySet().retainAll(in_params);

            parameters.putAll(out_params);
        }

        // use all the explicitly provided parameters
        parameters.putAll(parameters_);

        // handle an explicit path info
        if (pathInfo_ != null) {
            if (url.charAt(url.length() - 1) != '/') {
                url.append("/");
            }
            url.append(StringUtils.encodeUrl(pathInfo_, "/~"));
        }
        // handle path info mapping
        else if (route_.pathInfoHandling().type() == PathInfoType.MAP) {
            for (var mapping : route_.pathInfoHandling().mappings()) {
                if (parameters.keySet().containsAll(mapping.parameters())) {
                    var parameters_it = mapping.parameters().iterator();

                    var builder = new StringBuilder();
                    String parameter_name;
                    String[] parameter_value;
                    for (var segment : mapping.segments()) {
                        if (segment.isRegexp()) {
                            if (!parameters_it.hasNext()) {
                                continue;
                            }

                            parameter_name = parameters_it.next();

                            // ensure that the parameter has at least one value
                            parameter_value = parameters.get(parameter_name);
                            if (null == parameter_value ||
                                parameter_value.length < 1) {
                                continue;
                            }

                            // ensure that the parameter value corresponds to the
                            // regexp pattern for it
                            var matcher = segment.pattern().matcher(parameter_value[0]);
                            if (!matcher.matches()) {
                                continue;
                            }

                            // add the url-encoded parameter value to the path info
                            builder.append(StringUtils.encodeUrl(parameter_value[0]));
                            parameters.remove(parameter_name);
                        } else {
                            builder.append(segment.text());
                        }
                    }

                    // append the new path info
                    var path_info = builder.toString();
                    if (!path_info.isEmpty()) {
                        if (path_info.charAt(0) != '/' && url.charAt(url.length() - 1) != '/') {
                            url.append("/");
                        }
                        url.append(path_info);
                    }

                    break;
                }
            }
        }

        // generate the query parameters that are available
        if (parameters.size() > 0) {
            var query_parameters = new StringBuilder("?");

            for (var parameter_entry : parameters.entrySet()) {
                var parameter_name = parameter_entry.getKey();
                var parameter_values = parameter_entry.getValue();
                if (null == parameter_values) {
                    continue;
                }

                if (query_parameters.length() > 1) {
                    query_parameters.append("&");
                }

                for (var i = 0; i < parameter_values.length; i++) {
                    query_parameters.append(StringUtils.encodeUrl(parameter_name));
                    query_parameters.append("=");
                    query_parameters.append(StringUtils.encodeUrl(parameter_values[i]));
                    if (i + 1 < parameter_values.length) {
                        query_parameters.append("&");
                    }
                }
            }

            url.append(query_parameters);
        }

        if (fragment_ != null) {
            url.append("#");
            url.append(StringUtils.encodeUrl(fragment_));
        }

        return url.toString();
    }
}
