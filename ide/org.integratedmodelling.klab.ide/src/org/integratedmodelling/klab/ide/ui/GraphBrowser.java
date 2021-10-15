///*******************************************************************************
// * Copyright (C) 2007, 2015:
// * 
// * - Ferdinando Villa <ferdinando.villa@bc3research.org> - integratedmodelling.org - any
// * other authors listed in @author annotations
// *
// * All rights reserved. This file is part of the k.LAB software suite, meant to enable
// * modular, collaborative, integrated development of interoperable data and model
// * components. For details, see http://integratedmodelling.org.
// * 
// * This program is free software; you can redistribute it and/or modify it under the terms
// * of the Affero General Public License Version 3 or any later version.
// *
// * This program is distributed in the hope that it will be useful, but without any
// * warranty; without even the implied warranty of merchantability or fitness for a
// * particular purpose. See the Affero General Public License for more details.
// * 
// * You should have received a copy of the Affero General Public License along with this
// * program; if not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite
// * 330, Boston, MA 02111-1307, USA. The license is also available at:
// * https://www.gnu.org/licenses/agpl.html
// *******************************************************************************/
//package org.integratedmodelling.klab.ide.ui;
//
//import java.util.Map;
//import java.util.Map.Entry;
//
//import org.eclipse.jface.viewers.ISelectionChangedListener;
//import org.eclipse.jface.viewers.IStructuredSelection;
//import org.eclipse.jface.viewers.ITableLabelProvider;
//import org.eclipse.jface.viewers.ITreeContentProvider;
//import org.eclipse.jface.viewers.LabelProvider;
//import org.eclipse.jface.viewers.SelectionChangedEvent;
//import org.eclipse.jface.viewers.TableViewer;
//import org.eclipse.jface.viewers.Viewer;
//import org.eclipse.swt.SWT;
//import org.eclipse.swt.custom.SashForm;
//import org.eclipse.swt.events.ControlEvent;
//import org.eclipse.swt.events.ControlListener;
//import org.eclipse.swt.events.SelectionAdapter;
//import org.eclipse.swt.events.SelectionEvent;
//import org.eclipse.swt.graphics.Image;
//import org.eclipse.swt.layout.FillLayout;
//import org.eclipse.swt.layout.GridData;
//import org.eclipse.swt.layout.GridLayout;
//import org.eclipse.swt.widgets.Composite;
//import org.eclipse.swt.widgets.Control;
//import org.eclipse.swt.widgets.Display;
//import org.eclipse.swt.widgets.Table;
//import org.eclipse.swt.widgets.TableColumn;
//import org.eclipse.swt.widgets.ToolBar;
//import org.eclipse.swt.widgets.ToolItem;
//import org.eclipse.ui.forms.widgets.FormToolkit;
//import org.eclipse.wb.swt.ResourceManager;
//import org.eclipse.wb.swt.SWTResourceManager;
//import org.eclipse.zest.core.viewers.GraphViewer;
//import org.eclipse.zest.core.viewers.IGraphContentProvider;
//import org.eclipse.zest.core.widgets.ZestStyles;
//import org.eclipse.zest.layouts.LayoutAlgorithm;
//import org.eclipse.zest.layouts.algorithms.GridLayoutAlgorithm;
//import org.eclipse.zest.layouts.algorithms.RadialLayoutAlgorithm;
//import org.eclipse.zest.layouts.algorithms.SpringLayoutAlgorithm;
//import org.eclipse.zest.layouts.algorithms.TreeLayoutAlgorithm;
//import org.integratedmodelling.common.visualization.GraphVisualization;
//import org.integratedmodelling.kactors.kactors.Metadata;
//import org.integratedmodelling.klab.api.observations.IDirectObservation;
//import org.integratedmodelling.klab.api.observations.IRelationship;
//import org.integratedmodelling.klab.api.observations.ISubject;
//import org.integratedmodelling.klab.ide.Activator;
//
///**
// * Graph display widget, with hidden metadata table.
// * 
// * @author ferdinando.villa
// */
//public class GraphBrowser extends Composite {
//
//    private final static int  LAYOUT_SPRING = 0;
//    private final static int  LAYOUT_GRID   = 1;
//    private final static int  LAYOUT_HTREE  = 2;
//    private final static int  LAYOUT_VTREE  = 3;
//    private final static int  LAYOUT_RADIAL = 4;
//    private final FormToolkit toolkit       = new FormToolkit(Display.getCurrent());
//    private GraphViewer       graphViewer;
//    private SashForm          sashForm;
//    private TableViewer       tableViewer;
//    private Object            _graph;
//    private Table             table;
//    private Map<?, ?>         data          = null;
//
//    class ComponentLabelProvider extends LabelProvider implements ITableLabelProvider {
//
//        @Override
//        public Image getImage(Object element) {
//
//            // if (element instanceof Component) {
//            // ResourceManager.getPluginImage(Activator.PLUGIN_ID, "/icons/Plugin.png");
//            // }
//            return null;
//        }
//
//        @Override
//        public String getText(Object element) {
//            return null;
//        }
//
//        @Override
//        public Image getColumnImage(Object element, int columnIndex) {
//            // TODO Auto-generated method stub
//            return null;
//        }
//
//        @Override
//        public String getColumnText(Object element, int columnIndex) {
//            if (element instanceof Entry) {
//                if (columnIndex == 0) {
//                    return ((Entry<?, ?>) element).getKey().toString();
//                }
//                return ((Entry<?, ?>) element).getValue().toString();
//            }
//            return null;
//        }
//
//    }
//
//    class ComponentContentProvider implements ITreeContentProvider {
//
//        @Override
//        public void dispose() {
//        }
//
//        @Override
//        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
//        }
//
//        @Override
//        public Object[] getElements(Object inputElement) {
//            return getChildren(inputElement);
//        }
//
//        @Override
//        public Object[] getChildren(Object parentElement) {
//            if (parentElement instanceof Map) {
//                return data.entrySet().toArray();
//            }
//            return null;
//        }
//
//        @Override
//        public Object getParent(Object element) {
//            if (element instanceof Entry) {
//                return data;
//            }
//            return null;
//        }
//
//        @Override
//        public boolean hasChildren(Object element) {
//            return element instanceof Map && data.size() > 1;
//        }
//
//    }
//
//    public class GraphLabelProvider extends LabelProvider {
//
//        @Override
//        public String getText(Object element) {
//
//            if (element instanceof ISubject) {
//                return ((IDirectObservation) element).getName();
//            } else if (element instanceof GraphVisualization.Node) {
//                return ((GraphVisualization.Node) element).label;
//            } else if (element instanceof GraphVisualization.Edge) {
//                return ((GraphVisualization.Edge) element).label;
//            }
//
//            return null;
//        }
//
//        @Override
//        public Image getImage(Object element) {
//
//            if (element instanceof ISubject) {
//                return ResourceManager
//                        .getPluginImage(Activator.PLUGIN_ID, "icons/observer.gif");
//            } else if (element instanceof GraphVisualization.Node) {
//
//                String type = ((GraphVisualization.Node) element).type;
//
//                if (type.equals("processing-step")) {
//                    return ResourceManager
//                            .getPluginImage(Activator.PLUGIN_ID, "icons/sigma.gif");
//                } else if (type.equals("amodel")) {
//                    return ResourceManager
//                            .getPluginImage(Activator.PLUGIN_ID, "icons/agent.gif");
//                } else if (type.equals("dmodel")) {
//                    return ResourceManager
//                            .getPluginImage(Activator.PLUGIN_ID, "icons/model.gif");
//                } else if (type.equals("observer")) {
//                    // TODO different observer types
//                    return ResourceManager
//                            .getPluginImage(Activator.PLUGIN_ID, "icons/observer.png");
//                } else if (type.equals("datasource")) {
//                    return ResourceManager
//                            .getPluginImage(Activator.PLUGIN_ID, "icons/datasource.gif");
//                } else if (type.equals("state")) {
//                    return ResourceManager
//                            .getPluginImage(Activator.PLUGIN_ID, "icons/dataset.gif");
//                } else if (type.equals("networknode")) {
//                    return ResourceManager
//                            .getPluginImage(Activator.PLUGIN_ID, "icons/server.gif");
//                } else if (type.equals("clientnode")) {
//                    return ResourceManager
//                            .getPluginImage(Activator.PLUGIN_ID, "icons/Computer.png");
//                } else if (type.equals("user")) {
//                    return ResourceManager
//                            .getPluginImage(Activator.PLUGIN_ID, "icons/User.png");
//                } else if (type.equals("start")) {
//                    return ResourceManager
//                            .getPluginImage(Activator.PLUGIN_ID, "icons/start.gif");
//                } else if (type.equals("process")) {
//                    return ResourceManager
//                            .getPluginImage(Activator.PLUGIN_ID, "icons/process.png");
//                }
//            }
//
//            return super.getImage(element);
//        }
//    }
//
//    public class GraphContentProvider implements IGraphContentProvider {
//
//        @Override
//        public void dispose() {
//        }
//
//        @Override
//        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
//        }
//
//        @Override
//        public Object[] getElements(Object inputElement) {
//            if (inputElement instanceof ISubject) {
//                return ((ISubject) inputElement).getStructure().getRelationships()
//                        .toArray();
//            } else if (inputElement instanceof GraphVisualization) {
//                return ((GraphVisualization) inputElement).edgeSet().toArray();
//            }
//            return null;
//        }
//
//        @Override
//        public Object getSource(Object rel) {
//            if (rel instanceof IRelationship) {
//                return ((IRelationship) rel).getSource();
//            } else if (rel instanceof GraphVisualization.Edge) {
//                return ((GraphVisualization.Edge) rel).getSourceNode();
//            }
//            return null;
//        }
//
//        @Override
//        public Object getDestination(Object rel) {
//            if (rel instanceof IRelationship) {
//                return ((IRelationship) rel).getTarget();
//            } else if (rel instanceof GraphVisualization.Edge) {
//                return ((GraphVisualization.Edge) rel).getTargetNode();
//            }
//            return null;
//        }
//    }
//
//    public GraphBrowser(Composite parent, int style) {
//        super(parent, SWT.BORDER);
//        setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
//        setLayout(new GridLayout(1, false));
//
//        // if resized, layout from scratch.
//        this.addControlListener(new ControlListener() {
//
//            @Override
//            public void controlResized(ControlEvent e) {
//                graphViewer.setInput(_graph);
//                // for (Object c : graphViewer.getGraphControl().getNodes()) {
//                // GraphNode node = (GraphNode)c;
//                // Object ostia = node.getData();
//                // node.setTooltip(new org.eclipse.draw2d.Label("Zio bidone"));
//                // }
//                graphViewer.applyLayout();
//            }
//
//            @Override
//            public void controlMoved(ControlEvent e) {
//            }
//        });
//
//        ToolBar toolBar = new ToolBar(this, SWT.FLAT | SWT.RIGHT);
//        toolBar.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1));
//        toolkit.adapt(toolBar);
//        toolkit.paintBordersFor(toolBar);
//
//        ToolItem toolItem = new ToolItem(toolBar, SWT.RADIO);
//        toolItem.addSelectionListener(new SelectionAdapter() {
//            @Override
//            public void widgetSelected(SelectionEvent e) {
//                setLayoutEngine(LAYOUT_RADIAL);
//            }
//        });
//        toolItem.setImage(ResourceManager
//                .getPluginImage("org.integratedmodelling.thinkcap", "icons/radial_layout.gif"));
//
//        ToolItem toolItem_1 = new ToolItem(toolBar, SWT.RADIO);
//        toolItem_1.addSelectionListener(new SelectionAdapter() {
//            @Override
//            public void widgetSelected(SelectionEvent e) {
//                setLayoutEngine(LAYOUT_GRID);
//            }
//        });
//        toolItem_1.setImage(ResourceManager
//                .getPluginImage("org.integratedmodelling.thinkcap", "icons/grid_layout.gif"));
//
//        ToolItem toolItem_2 = new ToolItem(toolBar, SWT.RADIO);
//        toolItem_2.addSelectionListener(new SelectionAdapter() {
//            @Override
//            public void widgetSelected(SelectionEvent e) {
//                setLayoutEngine(LAYOUT_SPRING);
//            }
//        });
//        toolItem_2.setImage(ResourceManager
//                .getPluginImage("org.integratedmodelling.thinkcap", "icons/spring_layout.gif"));
//
//        ToolItem toolItem_3 = new ToolItem(toolBar, SWT.RADIO);
//        toolItem_3.setSelection(true);
//        toolItem_3.addSelectionListener(new SelectionAdapter() {
//            @Override
//            public void widgetSelected(SelectionEvent e) {
//                setLayoutEngine(LAYOUT_VTREE);
//            }
//        });
//        toolItem_3.setImage(ResourceManager
//                .getPluginImage("org.integratedmodelling.thinkcap", "icons/tree_layout.gif"));
//
//        ToolItem toolItem_4 = new ToolItem(toolBar, SWT.RADIO);
//        toolItem_4.addSelectionListener(new SelectionAdapter() {
//            @Override
//            public void widgetSelected(SelectionEvent e) {
//                setLayoutEngine(LAYOUT_HTREE);
//            }
//        });
//        toolItem_4.setImage(ResourceManager
//                .getPluginImage("org.integratedmodelling.thinkcap", "icons/horizontal_tree_layout.gif"));
//        toolkit.paintBordersFor(this);
//
//        sashForm = new SashForm(this, SWT.VERTICAL);
//        sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
//        toolkit.adapt(sashForm);
//        toolkit.paintBordersFor(sashForm);
//
//        graphViewer = new GraphViewer(sashForm, SWT.NONE);
//        Control control = graphViewer.getControl();
//        control.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
//        graphViewer.setLabelProvider(new GraphLabelProvider());
//        graphViewer.setContentProvider(new GraphContentProvider());
//        graphViewer.setConnectionStyle(ZestStyles.CONNECTIONS_DIRECTED);
//        graphViewer.setNodeStyle(ZestStyles.NODES_NO_LAYOUT_RESIZE
//                | ZestStyles.NODES_FISHEYE);
//
//        graphViewer.addSelectionChangedListener(new ISelectionChangedListener() {
//
//            @Override
//            public void selectionChanged(SelectionChangedEvent event) {
// 
//                Object o = ((IStructuredSelection) (event.getSelection())).getFirstElement();
//                if (o instanceof GraphVisualization.Node) {
//                    data = ((Metadata)((GraphVisualization.Node)o).getMetadata()).getDataAsMap();
//                } else if (o instanceof GraphVisualization.Edge) {
//                    data = ((Metadata)((GraphVisualization.Edge)o).getMetadata()).getDataAsMap();
//                }
//                if (event.getSelection().isEmpty()) {
//                    sashForm.setWeights(new int[]{1,0});
//                } else if (data != null && !data.isEmpty()){
//                    sashForm.setWeights(new int[]{2,1});
//                    tableViewer.setInput(data);
//                }
//            }
//        });
//
//        Composite composite = new Composite(sashForm, SWT.NONE);
//        toolkit.adapt(composite);
//        toolkit.paintBordersFor(composite);
//        composite.setLayout(new FillLayout(SWT.HORIZONTAL));
//
//        tableViewer = new TableViewer(composite, SWT.BORDER
//                | SWT.FULL_SELECTION);
//        table = tableViewer.getTable();
//        table.setLinesVisible(true);
//        toolkit.paintBordersFor(table);
//        
//        TableColumn tblclmnNewColumn = new TableColumn(table, SWT.NONE);
//        tblclmnNewColumn.setWidth(140);
//        tblclmnNewColumn.setText("New Column");
//        
//        TableColumn tblclmnNewColumn_1 = new TableColumn(table, SWT.NONE);
//        tblclmnNewColumn_1.setWidth(300);
//        tblclmnNewColumn_1.setText("New Column");
//
//        tableViewer.setContentProvider(new ComponentContentProvider());
//        tableViewer.setLabelProvider(new ComponentLabelProvider());
//        
//        sashForm.setWeights(new int[] { 1, 0 });
//        setLayoutEngine(LAYOUT_VTREE);
//
//    }
//
//    private void setLayoutEngine(int lcode) {
//
//        LayoutAlgorithm layout = null;
//
//        switch (lcode) {
//        case LAYOUT_SPRING:
//            layout = new SpringLayoutAlgorithm();
//            break;
//        case LAYOUT_VTREE:
//            layout = new TreeLayoutAlgorithm();
//            break;
//        case LAYOUT_GRID:
//            layout = new GridLayoutAlgorithm();
//            break;
//        case LAYOUT_HTREE:
//            layout = new TreeLayoutAlgorithm();
//            break;
//        case LAYOUT_RADIAL:
//            layout = new RadialLayoutAlgorithm();
//            break;
//        }
//
//        graphViewer.setLayoutAlgorithm(layout, true);
//        graphViewer.applyLayout();
//    }
//
//    public void show(Object gv) {
//        graphViewer.setInput(_graph = gv);
//        // for (Object c : graphViewer.getGraphControl().getNodes()) {
//        // GraphNode node = (GraphNode)c;
//        // Object ostia = node.getData();
//        // node.setTooltip(new org.eclipse.draw2d.Label("Zio bidone"));
//        // }
//        graphViewer.applyLayout();
//    }
//}
