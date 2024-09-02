package com.kautiainen.antti.rpgs.arm5.java.spelldesigner.resources;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

import com.kautiainen.antti.rpgs.arm5.java.spelldesigner.FormArtType;
import com.kautiainen.antti.rpgs.arm5.java.spelldesigner.FormInterface;
import com.kautiainen.antti.rpgs.arm5.java.spelldesigner.HermeticArts;
import com.kautiainen.antti.rpgs.arm5.java.spelldesigner.SpellGuideline;
import com.kautiainen.antti.rpgs.arm5.java.spelldesigner.TechniqueArtType;
import com.kautiainen.antti.rpgs.arm5.java.spelldesigner.TechniqueInterface;

import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

@Path("/api/guideline")
public class RestResource {
    
    
    @Context
    private UriInfo context;

    /**
     * The parser of levels.
     */
    public static final Function<String, SpellGuideline.GuidelineLevel> LEVEL_PARSER = new Function<>() {

        @Override
        public SpellGuideline.GuidelineLevel apply(String level) throws IllegalArgumentException  {
            return SpellGuideline.GuidelineLevel.valueOf(level);            
        }
    };

    /**
     * The comparator of arts.
     */
    public static final Comparator<String> ART_COMPARATOR = Comparator.nullsFirst(Comparator.naturalOrder());

    /**
     * The comparator of levels.
     */
    public static final Comparator<SpellGuideline.GuidelineLevel> LEVEL_COMPARATOR = (compared, comparee) -> (compared.compareTo(comparee));

    /**
     * Is the value empty or null.
     * @param value The tested value.
     * @return True, if and only if the value is either undefined or empty.
     */
    public static boolean isEmptyOrNull(String value) {
        return value == null || value.isEmpty();
    }

    /**
     * The comparator of guideline keys ignoring all empty key fields.
     */
    public static final Comparator<GuidelineKey> GUIDELINE_KEY_COMPARATOR = (compared, comparee) -> {
        int result = 0;
        if (compared != null && comparee != null) {
            if (result == 0 && isEmptyOrNull(compared.tech()) && isEmptyOrNull(comparee.tech())) {
                result = ART_COMPARATOR.compare(compared.tech(), comparee.tech());
            }
            if (result == 0 && isEmptyOrNull(compared.form()) && isEmptyOrNull(comparee.form())) {
                result = ART_COMPARATOR.compare(compared.form(), comparee.form());
            }
            if (result == 0 && isEmptyOrNull(compared.level()) && isEmptyOrNull(comparee.level())) {
                result = LEVEL_COMPARATOR.compare(LEVEL_PARSER.apply(compared.level()), LEVEL_PARSER.apply(comparee.level()));
            }
        }
        return result;
    };


    /**
     * The guildeline key used to 
     */
    public static record GuidelineKey(String tech, String form, String level) implements Comparable<GuidelineKey> {

        @Override
        public final int compareTo(GuidelineKey other) {
            return GUIDELINE_KEY_COMPARATOR.compare(this, other);
        }
    }

    /**
     * The spell guidelines cache.
     */
    private final ConcurrentNavigableMap<GuidelineKey, java.util.concurrent.CopyOnWriteArrayList<SpellGuidelineRecord>> guidelinesCache = new 
    ConcurrentSkipListMap<>(GUIDELINE_KEY_COMPARATOR);

    
    @GET
    @Path("/{tech}/{form}/{level}/{index}")
    public Optional<SpellGuidelineRecord> getSpellGuideline(@PathParam("tech") String tech, @PathParam("form") String form, @PathParam("level") String level, 
        @PathParam("index") String indexString) {
        GuidelineKey key = new GuidelineKey(tech, form, level);
        java.util.List<SpellGuidelineRecord> entries = guidelinesCache.get(key);
        try {
            int index = Integer.parseInt(indexString);
            if (entries == null || index < 0 || index >= entries.size()) {
                return Optional.empty();
            } else {
                return Optional.of(entries.get(index));
            }
        } catch(NumberFormatException nfe) {
            throw new IllegalArgumentException("Invalid index", nfe);
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{tech}/{form}/{level}")
    public SpellGuidelineRecord[] getAllGuidelines(@PathParam("tech") String tech, @PathParam("form") String form, @PathParam("level") String level) {
        if (tech == null || tech.isEmpty()) {
            throw new IllegalArgumentException("Invalid technique name");
        }
        if (form == null || form.isEmpty()) {
            throw new IllegalArgumentException("Invalid form name");
        }
        GuidelineKey filter = new GuidelineKey(tech, form, level);
        java.util.List<SpellGuidelineRecord> result = guidelinesCache.entrySet().stream().filter(
            entry -> (entry.getKey().equals(filter))
        ).collect(
            () -> (new java.util.ArrayList<SpellGuidelineRecord>()), 
            (List<SpellGuidelineRecord> list, java.util.Map.Entry<GuidelineKey, CopyOnWriteArrayList<SpellGuidelineRecord>> entry) -> {list.addAll(entry.getValue());},
            (List<SpellGuidelineRecord> head, List<SpellGuidelineRecord> tail) -> {head.addAll(tail);}
        );
        SpellGuidelineRecord[] res = new SpellGuidelineRecord[0];
        return result.toArray(res);
    }

    @POST
    @Path("/{tech}/{form}/{level}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response spellGuideline(@PathParam("tech") String tech, @PathParam("form") String form, @PathParam("level") String level, JsonObject guideline) {
        if (isEmptyOrNull(tech) || isEmptyOrNull(form) || isEmptyOrNull(level)) {
            // Return error.
            throw new IllegalArgumentException("Invalid requiest parameters");
        } else {
            if (guideline.get("name") == null || guideline.get("name").getValueType() != JsonValue.ValueType.STRING) {
                throw new IllegalArgumentException("invalid request body");
            }
            String name = ((JsonString)guideline.get("name")).getString();
            String description = null;
            if (guideline.get("description") != null) {
                if (guideline.get("description").getValueType() != JsonValue.ValueType.STRING) {
                    throw new IllegalArgumentException("Invalid request body - invalid description");
                } else {
                    description = ((JsonString)guideline.get("description")).getString();
                }
            }

            GuidelineKey key = new GuidelineKey(tech, form, level);
            CopyOnWriteArrayList<SpellGuidelineRecord> guidelines; 
            if (guidelinesCache.containsKey(key)) {
                guidelines = guidelinesCache.get(key);
            } else {
                guidelines = new CopyOnWriteArrayList<>();
            }
            SpellGuidelineRecord record = new SpellGuidelineRecord(
                parseTechnique(tech), 
                parseForm(form),
                SpellGuideline.GuidelineLevel.valueOf(level), 
            name, 
            description);
            if (guidelines.stream().filter( current -> (current.name().equals(name))).findAny().isPresent()) {
                // The guideline already exists.
                throw new IllegalArgumentException("Cannot insert an existing guideline");
            } else {
                // Addign new guideline.
                guidelines.add(record);
            }
            guidelinesCache.put(key, guidelines);

            return Response.created(context.getRequestUri()).build();
        }
    }

    /**
     * Parse form form from name.
     *
     * @param form The parsed form name.
     * @return The form with given name.
     * @throws NotFoundException The form does not exist.
     */
    public FormInterface<FormArtType> parseForm(String form) throws NotFoundException {
        return HermeticArts.DEFAULT_ARTS.getForm(form).orElseThrow( () -> (new NotFoundException("No such form exists")));
    }

    /**
     * Parse technique from name.
     *
     * @param tech The pared technqiue name.
     * @return The technqiue with given name.
     * @throws NotFoundException The technique does not exist.
     */
    public TechniqueInterface<TechniqueArtType> parseTechnique(String tech) throws NotFoundException {
        return HermeticArts.DEFAULT_ARTS.getTechnique(tech).orElseThrow( () -> (new NotFoundException("No such technique exists")));
    }
}
