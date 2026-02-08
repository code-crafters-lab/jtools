package org.codecrafterslab.agent.plugin.cs;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.codecrafterslab.agent.core.plugin.BasePluginConfiguration;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class ConstSubstitutionPluginConfiguration extends BasePluginConfiguration {

    List<ConstSubstitutionRule<String>> rules;

}
